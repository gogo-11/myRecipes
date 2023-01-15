package com.myrecipe.entities.requests;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Users;
@Data
@NoArgsConstructor
public class RecipesRequest {
    @NotNull
    @NotBlank
    private String recipeName;

    @NotNull
    @NotBlank
    private String products;

    @NotNull
    @NotBlank
    private Integer portions;

    @NotNull
    @NotBlank
    private Integer cookingTime;

    @NotNull
    @NotBlank
    private String cookingSteps;

    @NotNull
    @NotBlank
    @Enumerated(EnumType.STRING)
    private Categories category;

    @NotNull
    @NotBlank
    private Boolean isPrivate;

    @NotNull
    @NotBlank
    private Integer userId;
}
