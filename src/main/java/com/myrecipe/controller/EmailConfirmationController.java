package com.myrecipe.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.myrecipe.entities.EmailConfirmationToken;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.EmailConfirmationTokenRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.security.SecurityService;
import com.myrecipe.service.EmailConfirmationService;
import com.myrecipe.service.UsersService;

@Controller
public class EmailConfirmationController {
    public static final String LOGIN_FORM_PAGE = "login_form";
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private EmailConfirmationService emailService;
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${app.base-url}")
    private String appBaseUrl;
    @Value("${spring.mail.username}")
    private String username;
    private static final String ERROR_ATTRIBUTE_NAME = "error";


    @PostMapping("/send-confirmation-email")
    public String sendConfirmationEmail (@ModelAttribute UsersRequest request, Model model) {
        if(securityService.isAuthenticated()) {
            return "redirect:/";
        }

        EmailConfirmationToken emailToken = null;

        Users userByEmail;

        try {
            userByEmail = usersService.getByEmail(request.getEmail());
        } catch (RecordNotFoundException e) {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Няма открит акаунт, асоцииран с този имейл!");
            return "registration";
        }

        try {
            emailToken = emailService.getByUserId(usersService.getByEmail(request.getEmail()).getId());
        } catch (RecordNotFoundException e) {
            System.out.println(e.getMessage());
        }

        if(emailToken != null) {
            emailService.deleteToken(emailToken.getId());
        }

        String token = UUID.randomUUID().toString();

        EmailConfirmationTokenRequest emailRequest = new EmailConfirmationTokenRequest();
        emailRequest.setToken(token);
        emailRequest.setUserId(userByEmail.getId());

        emailService.createNewEmailConfirmationToken(emailRequest);

        String emailConfirmationLink = appBaseUrl + "/confirm-email/" + token;
        sendEmailConfirmationLink(request.getEmail(), emailConfirmationLink);

        model.addAttribute("emailSent", "Изпратен Ви бе имейл за потвърждениe. Проверете входящата си поща!");

        return LOGIN_FORM_PAGE;
    }

    @RequestMapping(value = "/confirm-email/{token}", method = { RequestMethod.GET, RequestMethod.POST })
    public String confirmEmail (@PathVariable("token") String token, Model model) {
        if(securityService.isAuthenticated()) {
            return "redirect:/";
        }
        EmailConfirmationToken emailToken = null;

        try {
            emailToken = emailService.getByToken(token);
        } catch (RecordNotFoundException e) {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Невалиден токен!");
            return LOGIN_FORM_PAGE;
        }

        Users userByToken;
        try {
            userByToken = usersService.getById(emailToken.getUser().getId());
        } catch (RecordNotFoundException e) {
            model.addAttribute(ERROR_ATTRIBUTE_NAME, "Няма открит акаунт, асоцииран с този имейл!");
            return LOGIN_FORM_PAGE;
        }

        UsersRequest userRequest = new UsersRequest();
        userRequest.setActivated(true);
        userByToken.setEmailConfirmationToken(null);
        this.usersRepository.save(userByToken);
        usersService.userUpdate(userByToken.getId(), userRequest);
        model.addAttribute("success", "Потвърдихте имейл адреса си успешно!");

        return LOGIN_FORM_PAGE;
    }

    private void sendEmailConfirmationLink(String email, String emailConfirmationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(email);
        message.setSubject("Потвърждение на имейл");
        message.setText("За да потвърдите имейла си, моля последвайте следния линк:\n\n" + emailConfirmationLink);

        javaMailSender.send(message);
    }
}
