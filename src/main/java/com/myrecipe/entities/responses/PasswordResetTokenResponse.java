package com.myrecipe.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetTokenResponse {
    private String message;
}
