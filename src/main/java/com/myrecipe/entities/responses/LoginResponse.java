package com.myrecipe.entities.responses;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String tokenType;
    private String accessToken;
    private Instant expiresAt;
    private AuthenticatedUserResponse user;
}
