package com.myrecipe.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import com.myrecipe.entities.RolesEn;
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

        if(!isUser()){
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

        if (!isUser())
            return "redirect:/";

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

    @GetMapping("/reset-password")
    public String showResetPassForm (Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("showResPass", "Showing reset password form");

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPasswordForm(@RequestParam("email") String email, Model model) {
        if(securityService.isAuthenticated()){
            return "redirect:/";
        }

        Users userByEmail;

        try {
            userByEmail = usersService.getByEmail(email);
        } catch (RecordNotFoundException e) {
            model.addAttribute("notFound", "Няма открит акаунт, асоцииран с този имейл!");
            return "reset-password";
        }

        PasswordResetToken resetToken = null;

        try {
            resetToken = resetPasswordService.getByUserId(userByEmail.getId());
        } catch (RecordNotFoundException e) {
            model.addAttribute("error", "Вече сте заявили нулиране на паролата. Моля, проверете имейла си!");
            return "reset-password";
        }

        if(resetToken != null) {
            model.addAttribute("error", "Вече сте заявили нулиране на паролата. Моля, проверете имейла си!");
            return "reset-password";
        }

        String token = UUID.randomUUID().toString();

        // Create a new password reset token request
        PasswordResetTokenRequest request = new PasswordResetTokenRequest();
        request.setToken(token);
        request.setUserId(userByEmail.getId());
        request.setExpirationDateTime(LocalDateTime.now().plusHours(4));

        resetPasswordService.createNewResetPasswordToken(request);

        String resetPasswordLink = appBaseUrl + "/new-password/" + token;
        sendResetPasswordEmail(email, resetPasswordLink);

        model.addAttribute("emailSent", "Изпратен ви бе имейл за възстановяване на паролата. Проверете входящата си поща!");
        return "reset-password";
    }

    @GetMapping("/new-password/{token}")
    public String verifyTokenAndShowUpdatePassForm (@PathVariable("token") String token, Model model) {
        if(securityService.isAuthenticated()){
            return "redirect:/";
        }

        PasswordResetToken passwordResetToken;
        try {
            passwordResetToken = resetPasswordService.getByToken(token);
        } catch (RecordNotFoundException e) {
            model.addAttribute("tokenError", "Невалиден токен!");
            return "new-password";
        }

        Users userByToken;
        try {
            userByToken = usersService.getById(passwordResetToken.getUser().getId());
        } catch (RecordNotFoundException e) {
            model.addAttribute("tokenError", "Няма открит акаунт, асоцииран с този имейл!");
            return "new-password";
        }

        if(userByToken == null) {
            return "redirect:/";
        }

        return "new-password";
    }

    @PostMapping("/new-password/{token}")
    public String submitNewPassword (@PathVariable("token") String token, @ModelAttribute(name="request") UsersRequest request, Model model) {
        if(securityService.isAuthenticated()){
            return "redirect:/";
        }

        PasswordResetToken passwordResetToken;
        try {
            passwordResetToken = resetPasswordService.getByToken(token);
        } catch (RecordNotFoundException e) {
            model.addAttribute("tokenError", "Невалиден токен!");
            return "new-password";
        }

        Users userByToken;
        try {
            userByToken = usersService.getById(passwordResetToken.getUser().getId());
        } catch (RecordNotFoundException e) {
            model.addAttribute("notFound", "Няма открит акаунт, асоцииран с този имейл!");
            return "new-password";
        }

        if(userByToken == null) {
            return "new-password";
        }

        if(!request.getEmail().equals(userByToken.getEmail())) {
            model.addAttribute("tokenError", "Въвели сте грешен имейл");
            return "new-password";
        }

        if(passwordResetToken.isExpired()){
            userByToken.setPasswordResetToken(null);
            this.usersRepository.save(userByToken);
            model.addAttribute("tokenError", "Токенът ви е изтекъл! Ако все още искате да нулирате паролата си, подайте нова заявка!");
            return "new-password";
        }

        userByToken.setPasswordResetToken(null);
        this.usersRepository.save(userByToken);
        this.usersService.userUpdate(userByToken.getId(), request);
        model.addAttribute("resetSuccess", "Променихте паролата си успешно");

        return "new-password";
    }

    private void sendResetPasswordEmail(String email, String resetPasswordLink) {
        // Create and configure your email message using JavaMailSender
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("goshanski.n@gmail.com");
        message.setTo(email);
        message.setSubject("Password Reset");
        message.setText("За да нулирате паролата си, моля кликнете на следния линк:\n\n"
                + resetPasswordLink
                +"\n\nЛинкът ще бъде активен в следващите 4 часа.");

        javaMailSender.send(message);
    }

    private boolean isUser() {
        if(securityService.isAuthenticated()){
            return usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.USER);
        } else
            return  false;
    }
}
