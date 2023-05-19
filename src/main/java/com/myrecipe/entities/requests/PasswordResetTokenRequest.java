package com.myrecipe.entities.requests;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenRequest {
    @NotBlank
    @NotNull
    private String token;

    @NotNull
    private Integer userId;

    @NotNull
    @NotBlank
    private LocalDateTime expirationDateTime;
}
