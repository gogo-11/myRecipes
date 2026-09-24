package com.myrecipe.service;

import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.ConfirmEmailRequest;
import com.myrecipe.entities.requests.RegisterRequest;
import com.myrecipe.entities.requests.ResendEmailConfirmationRequest;
import com.myrecipe.entities.responses.EmailConfirmationMessageResponse;
import com.myrecipe.entities.responses.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    Users getCurrentUser(String email);
    EmailConfirmationMessageResponse confirmEmail(ConfirmEmailRequest request);
    EmailConfirmationMessageResponse resendConfirmationEmail(ResendEmailConfirmationRequest request, String requestIp);
}
