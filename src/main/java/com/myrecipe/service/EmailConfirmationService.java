package com.myrecipe.service;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.EmailConfirmationToken;
import com.myrecipe.entities.requests.EmailConfirmationTokenRequest;
import com.myrecipe.entities.responses.EmailConfirmationResponse;

@Component
public interface EmailConfirmationService {

    EmailConfirmationToken getByToken (String token);
    EmailConfirmationToken getByUserId(Integer userId);
    EmailConfirmationResponse createNewEmailConfirmationToken(EmailConfirmationTokenRequest request);
    EmailConfirmationToken getByTokenId(Integer id);
    void deleteToken(Integer id);
}
