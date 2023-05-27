package com.myrecipe.entities.requests;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.myrecipe.entities.RolesEn;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRequest {
    @NotBlank(message = "Въведете име!")
    @NotNull(message = "Въведете име!")
    @Pattern(regexp = "^[а-яА-Я'-]+$", message = "Използвайте кирилица за име!")
    private String firstName;

    @NotBlank(message = "Въведете фамилия!")
    @NotNull(message = "Въведете фамилия!")
    @Pattern(regexp = "^[а-яА-Я'-]+$", message = "Използвайте кирилица за фамилия!")
    private String lastName;

    @NotBlank(message = "Въведете имейл!")
    @NotNull(message = "Въведете имейл!")
    @Email(message = "Невалиден формат за имейл адрес!")
    private String email;

    @NotBlank(message = "Може да използвате главни и малки букви, цифри и символите ~`!@#$%^&*()_-+={[}]|\\:;\"'<,>.?/")
    @NotNull(message = "Може да използвате главни и малки букви, цифри и символите ~`!@#$%^&*()_-+={[}]|\\:;\"'<,>.?/")
    @Size(min = 6, message = "Паролата трябва да е с дължина от поне 6 символа!")
    private String password;

    @Enumerated(EnumType.STRING)
    private RolesEn role;

    private boolean isActivated;
}
