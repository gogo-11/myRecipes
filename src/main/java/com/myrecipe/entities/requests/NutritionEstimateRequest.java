package com.myrecipe.entities.requests;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionEstimateRequest {
    @NotBlank
    private String recipeName;

    @NotBlank
    private String products;

    @NotNull
    @Min(1)
    private Integer portions;

    private String cookingSteps;
}
