package com.myrecipe.service;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.PasswordResetToken;
import com.myrecipe.entities.requests.PasswordResetTokenRequest;
import com.myrecipe.entities.responses.PasswordResetTokenResponse;

@Component
public interface ResetPasswordService {
    PasswordResetToken getByToken (String token);
    PasswordResetToken getByUserId(Integer userId);
    PasswordResetTokenResponse createNewResetPasswordToken(PasswordResetTokenRequest request);
    PasswordResetToken getByTokenId(Integer id);
    void deleteToken(Integer id);
}
