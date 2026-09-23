package com.myrecipe.service;

import java.util.Locale;
import java.util.UUID;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.EmailConfirmationTokenRequest;
import com.myrecipe.entities.requests.RegisterRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.RegisterResponse;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.entities.EmailConfirmationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MyAuthService implements AuthService {
    private final UsersService usersService;
    private final EmailConfirmationService emailConfirmationService;
    private final JavaMailSender javaMailSender;
    private final String appBaseUrl;
    private final String mailUsername;

    public MyAuthService(UsersService usersService,
                         EmailConfirmationService emailConfirmationService,
                         JavaMailSender javaMailSender,
                         @Value("${app.base-url}") String appBaseUrl,
                         @Value("${spring.mail.username}") String mailUsername) {
        this.usersService = usersService;
        this.emailConfirmationService = emailConfirmationService;
        this.javaMailSender = javaMailSender;
        this.appBaseUrl = appBaseUrl;
        this.mailUsername = mailUsername;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        UsersRequest usersRequest = new UsersRequest();
        usersRequest.setFirstName(request.getFirstName().trim());
        usersRequest.setLastName(request.getLastName().trim());
        usersRequest.setEmail(normalizeEmail(request.getEmail()));
        usersRequest.setPassword(request.getPassword());
        usersRequest.setRole(RolesEn.USER);
        usersRequest.setActivated(false);

        usersService.createUser(usersRequest);
        Users user = usersService.getByEmail(usersRequest.getEmail());
        createConfirmationTokenAndSendEmail(user);

        return new RegisterResponse("Registration successful. Please confirm your email before logging in.");
    }

    @Override
    public Users getCurrentUser(String email) {
        return usersService.getByEmail(email);
    }

    private void createConfirmationTokenAndSendEmail(Users user) {
        try {
            EmailConfirmationToken existingToken = emailConfirmationService.getByUserId(user.getId());
            emailConfirmationService.deleteToken(existingToken.getId());
        } catch (RecordNotFoundException e) {
            // No previous token exists for a newly registered user.
        }

        String token = UUID.randomUUID().toString();
        EmailConfirmationTokenRequest tokenRequest = new EmailConfirmationTokenRequest();
        tokenRequest.setToken(token);
        tokenRequest.setUserId(user.getId());
        emailConfirmationService.createNewEmailConfirmationToken(tokenRequest);

        sendEmailConfirmationLink(user.getEmail(), appBaseUrl + "/confirm-email/" + token);
    }

    private void sendEmailConfirmationLink(String email, String emailConfirmationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(email);
        message.setSubject("Потвърждение на имейл");
        message.setText("За да потвърдите имейла си, моля последвайте следния линк:\n\n" + emailConfirmationLink);

        javaMailSender.send(message);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
