package com.myrecipe.service;

import com.myrecipe.entities.PasswordResetToken;
import com.myrecipe.entities.requests.PasswordResetTokenRequest;
import com.myrecipe.entities.responses.PasswordResetTokenResponse;
import org.springframework.stereotype.Component;

@Component
public interface ResetPasswordService {
    PasswordResetToken getByToken (String token);
    PasswordResetToken getByUserId(Integer userId);
    PasswordResetTokenResponse createNewResetPasswordToken(PasswordResetTokenRequest request);
    PasswordResetToken getByTokenId(Integer id);
    void deleteToken(Integer id);
}
