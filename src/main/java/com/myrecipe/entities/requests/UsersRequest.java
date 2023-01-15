package com.myrecipe.entities.requests;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.myrecipe.entities.RolesEn;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRequest {
    @NotBlank
    @NotNull
    private String firstName;

    @NotBlank
    @NotNull
    private String lastName;

    @NotBlank
    @NotNull
    private String email;

    @NotBlank
    @NotNull
    private String password;

    @Enumerated(EnumType.STRING)
    private RolesEn role;
}
