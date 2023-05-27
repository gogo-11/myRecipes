package com.myrecipe.entities.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfirmationTokenRequest {

    @NotNull
    @NotBlank
    private String token;

    @NotNull
    private Integer userId;
}
