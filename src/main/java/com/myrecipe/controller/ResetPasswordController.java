package com.myrecipe.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.myrecipe.entities.PasswordResetToken;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.PasswordResetTokenRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.security.SecurityService;
import com.myrecipe.service.ResetPasswordService;
import com.myrecipe.service.UsersService;

@Controller
public class ResetPasswordController {
    @Autowired
    private SecurityService securityService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ResetPasswordService resetPasswordService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private PasswordEncoder encoder;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @GetMapping("/account/change-password")
    public String showChangePassForm(Model model) {
        if(!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("showResPass", "Showing reset password form");

        return "forward:/account";
    }

    @PostMapping("/account/change-password")
    public String updateUserPassword (@RequestParam("oldPass") String oldPass,
                                      @RequestParam("confirmPass") String confirmPass,
                                      @ModelAttribute UsersRequest request, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        Users user = usersService.getByEmail(securityService.getAuthentication());
        model.addAttribute("currentUser", user);
        model.addAttribute("showResPass", "Showing reset password form");

        if(oldPass.isBlank() || oldPass == null || confirmPass.isBlank() || confirmPass == null || request.getPassword().isBlank()) {
            model.addAttribute("errPass", "Попълнете всички полета!");
            model.addAttribute("currentUser", user);
            return "account";
        }

        if(!request.getPassword().equals(confirmPass)) {
            model.addAttribute("errPass", "Паролите не съвпадат!");
            return "account";
        }

        if (!encoder.matches(oldPass, user.getPassword())) {
            model.addAttribute("errPass", "Грешна текуща парола парола!");
            return "account";
        }

        usersService.userUpdate(user.getId(), request);
        model.addAttribute("changeSuccess", "Успешна смяна на паролата");

        return "account";
    }

    @GetMapping("/account/reset-password")
    public String showResetPassForm (Model model) {
        if(!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("showResPass", "Showing reset password form");

        return "forward:/account";
    }

    @PostMapping("/account/reset-password")
    public String processResetPasswordForm(@RequestParam("email") String email, Model model) {
        if(!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        // Validate the email and retrieve the user from your user repository
        String currentUserEmail = securityService.getAuthentication();
        if(!currentUserEmail.equals(email)){
            return "redirect:/account";
        }
        Users userByEmail = null;
        try {
            userByEmail = usersService.getByEmail(currentUserEmail);
        } catch (RecordNotFoundException e) {
            model.addAttribute("notFound", "Няма открит акаунт, асоцииран с този имейл!");
            return "forward:/account";
        }
//        Users user = usersService.getByEmail(currentUserEmail);
        if (userByEmail == null) {
            return "redirect:/account";
        }

        String token = UUID.randomUUID().toString();

        // Create a new password reset token request
        PasswordResetTokenRequest request = new PasswordResetTokenRequest();
        request.setToken(token);
        request.setUserId(userByEmail.getId());
        request.setExpirationDateTime(LocalDateTime.now().plusHours(4));

        resetPasswordService.createNewResetPasswordToken(request);

        String resetPasswordLink = appBaseUrl + "/new-password/" + token;
        sendResetPasswordEmail(currentUserEmail, resetPasswordLink);

        return "forward:/account";
    }

    @GetMapping("/new-password/{token}")
    public String verifyTokenAndShowUpdatePassForm (@PathVariable("token") String token, Model model) {
        PasswordResetToken passwordResetToken;
        try {
            passwordResetToken = resetPasswordService.getByToken(token);
        } catch (RecordNotFoundException e) {
            model.addAttribute("tokenError", e.getMessage());
            return "redirect:/new-password";
        }

        Users userByToken = usersService.getById(passwordResetToken.getUser().getId());
        if(userByToken == null) {
            return "redirect:/";
        }

        return "new-password";
    }

    @PostMapping("/new-password/{token}")
    public String submitNewPassword (@PathVariable("token") String token, @ModelAttribute(name="request") UsersRequest request, Model model) {
        PasswordResetToken passwordResetToken;
        try {
            passwordResetToken = resetPasswordService.getByToken(token);
        } catch (RecordNotFoundException e) {
            model.addAttribute("tokenError", e.getMessage());
            return "new-password";
        }

        Users userByToken = usersService.getById(passwordResetToken.getUser().getId());
        if(userByToken == null) {
            return "redirect:/";
        }

        if(passwordResetToken.isExpired()){
            resetPasswordService.deleteToken(passwordResetToken.getId());
            return "redirect:/";
        }

        request.setPassword(encoder.encode(request.getPassword()));

        userByToken.setPasswordResetToken(null);
        this.usersRepository.save(userByToken);
        this.usersService.userUpdate(userByToken.getId(), request);
        model.addAttribute("resetSuccess", "Променихте паролата си успешно");
        return "redirect:/account";
    }

    private void sendResetPasswordEmail(String email, String resetPasswordLink) {
        // Create and configure your email message using JavaMailSender
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("goshanski.n@gmail.com");
        message.setTo(email);
        message.setSubject("Password Reset");
        message.setText("За да нулирате паролата си, моля кликнете на следния линк:\n\n" + resetPasswordLink);

        // Send the email
        javaMailSender.send(message);
    }
}
