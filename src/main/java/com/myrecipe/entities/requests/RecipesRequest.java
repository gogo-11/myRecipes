package com.myrecipe.entities.requests;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.myrecipe.entities.Categories;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class RecipesRequest {
    @NotNull
    @NotBlank
    @Pattern(regexp = "[\\p{IsCyrillic}\\p{Punct}\\p{Zs}]+", message = "Използвайте кирилица за име!")
    private String recipeName;

    @NotNull
    @NotBlank
    @Pattern(regexp = "[\\p{IsCyrillic}\\p{Punct}\\p{Zs}]+", message = "Използвайте кирилица за продукти!")
    private String products;

    @NotNull
    @Min(value = 1, message = "Моля въведете валиден брой порции")
    private Integer portions;

    @NotNull
    @Min(value = 1, message = "Моля въведете валидно време за приготвяне")
    private Integer cookingTime;

    @NotNull
    @NotBlank
    @Pattern(regexp = "[\\p{IsCyrillic}\\p{Punct}\\p{Zs}]+", message = "Използвайте кирилица за стъпките на приготвяне!")
    private String cookingSteps;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Categories category;

    private Boolean isPrivate;

//    @NotNull
    private Integer userId;

    @NotNull
//    @NotEmpty
//    private byte[] image;
    private MultipartFile image;
}
