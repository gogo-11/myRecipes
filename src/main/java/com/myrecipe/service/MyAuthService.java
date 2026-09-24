package com.myrecipe.service;

import java.util.Locale;
import java.util.UUID;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.ConfirmEmailRequest;
import com.myrecipe.entities.requests.EmailConfirmationTokenRequest;
import com.myrecipe.entities.requests.RegisterRequest;
import com.myrecipe.entities.requests.ResendEmailConfirmationRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.EmailConfirmationMessageResponse;
import com.myrecipe.entities.responses.RegisterResponse;
import com.myrecipe.exceptions.InvalidEmailConfirmationTokenException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.entities.EmailConfirmationToken;
import com.myrecipe.repository.EmailConfirmationTokenRepository;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.security.EmailConfirmationResendThrottle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyAuthService implements AuthService {
    private static final String RESEND_RESPONSE_MESSAGE =
            "If the account exists and still needs confirmation, a confirmation email will be sent.";
    private static final String INVALID_CONFIRMATION_TOKEN_MESSAGE = "Invalid confirmation token.";

    private final UsersService usersService;
    private final EmailConfirmationService emailConfirmationService;
    private final UsersRepository usersRepository;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final JavaMailSender javaMailSender;
    private final EmailConfirmationResendThrottle resendThrottle;
    private final String frontendBaseUrl;
    private final String mailUsername;

    public MyAuthService(UsersService usersService,
                         EmailConfirmationService emailConfirmationService,
                         UsersRepository usersRepository,
                         EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                         JavaMailSender javaMailSender,
                         EmailConfirmationResendThrottle resendThrottle,
                         @Value("${app.frontend-base-url}") String frontendBaseUrl,
                         @Value("${spring.mail.username}") String mailUsername) {
        this.usersService = usersService;
        this.emailConfirmationService = emailConfirmationService;
        this.usersRepository = usersRepository;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.javaMailSender = javaMailSender;
        this.resendThrottle = resendThrottle;
        this.frontendBaseUrl = frontendBaseUrl;
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

    @Override
    @Transactional
    public EmailConfirmationMessageResponse confirmEmail(ConfirmEmailRequest request) {
        EmailConfirmationToken emailToken = emailConfirmationTokenRepository
                .findForUpdateByToken(request.getToken())
                .orElseThrow(() -> new InvalidEmailConfirmationTokenException(INVALID_CONFIRMATION_TOKEN_MESSAGE));

        Users user = emailToken.getUser();
        user.setActivated(true);
        user.setEmailConfirmationToken(null);
        usersRepository.save(user);
        emailConfirmationTokenRepository.delete(emailToken);

        return new EmailConfirmationMessageResponse("Email confirmed successfully.");
    }

    @Override
    @Transactional
    public EmailConfirmationMessageResponse resendConfirmationEmail(ResendEmailConfirmationRequest request,
                                                                   String requestIp) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (!resendThrottle.isAllowed(normalizedEmail, requestIp)) {
            return resendResponse();
        }

        Users user = usersRepository.findByEmail(normalizedEmail);
        if (user == null || user.isActivated()) {
            return resendResponse();
        }

        EmailConfirmationToken token = emailConfirmationTokenRepository
                .findOptionalByUserId(user.getId())
                .orElseGet(() -> createEmailConfirmationToken(user));

        try {
            sendEmailConfirmationLink(user.getEmail(), buildFrontendConfirmationLink(token.getToken()));
        } catch (MailException e) {
            return resendResponse();
        }

        return resendResponse();
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

        sendEmailConfirmationLink(user.getEmail(), buildFrontendConfirmationLink(token));
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

    private EmailConfirmationToken createEmailConfirmationToken(Users user) {
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        return emailConfirmationTokenRepository.save(token);
    }

    private String buildFrontendConfirmationLink(String token) {
        return frontendBaseUrl + "/confirm-email/" + token;
    }

    private EmailConfirmationMessageResponse resendResponse() {
        return new EmailConfirmationMessageResponse(RESEND_RESPONSE_MESSAGE);
    }
}
