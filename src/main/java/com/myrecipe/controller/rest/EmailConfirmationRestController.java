package com.myrecipe.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.myrecipe.entities.requests.ConfirmEmailRequest;
import com.myrecipe.entities.requests.ResendEmailConfirmationRequest;
import com.myrecipe.entities.responses.EmailConfirmationMessageResponse;
import com.myrecipe.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email-confirmations")
public class EmailConfirmationRestController {
    private final AuthService authService;

    public EmailConfirmationRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<EmailConfirmationMessageResponse> confirmEmail(
            @Valid @RequestBody ConfirmEmailRequest request) {
        return new ResponseEntity<>(authService.confirmEmail(request), HttpStatus.OK);
    }

    @PostMapping("/resend")
    public ResponseEntity<EmailConfirmationMessageResponse> resendConfirmationEmail(
            @Valid @RequestBody ResendEmailConfirmationRequest request,
            HttpServletRequest servletRequest) {
        return new ResponseEntity<>(
                authService.resendConfirmationEmail(request, servletRequest.getRemoteAddr()),
                HttpStatus.ACCEPTED);
    }
}
