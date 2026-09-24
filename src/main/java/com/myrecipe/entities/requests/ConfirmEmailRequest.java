package com.myrecipe.entities.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmEmailRequest {
    @NotBlank(message = "Token is required")
    @NotNull(message = "Token is required")
    private String token;
}
