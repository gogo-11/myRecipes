package com.myrecipe.entities.requests;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.myrecipe.entities.Categories;

@Data
@NoArgsConstructor
public class RecipesRequest {
    @NotNull
    @NotBlank
    @Pattern(regexp = "[\\p{IsCyrillic}\\p{Punct}\\p{Zs}]+", message = "Използвайте кирилица за име!")
    private String recipeName;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?s)(?U)[\\p{IsCyrillic}\\p{P}\\p{S}\\p{Zs}\\p{N}\\r\\n]+$", message = "Използвайте кирилица за продукти!")
    private String products;

    @NotNull
    @Min(value = 1, message = "Моля въведете валиден брой порции")
    private Integer portions;

    @NotNull
    @Min(value = 1, message = "Моля въведете валидно време за приготвяне")
    private Integer cookingTime;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?s)(?U)[\\p{IsCyrillic}\\p{P}\\p{S}\\p{Zs}\\p{N}\\r\\n]+$", message = "Използвайте кирилица за стъпките на приготвяне!")
    private String cookingSteps;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Categories category;

    private Boolean isPrivate;

    private Integer userId;

    @NotNull
    private MultipartFile image;
}
