package com.myrecipe.service;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.responses.NutritionEstimateResponse;

@Component
public interface NutritionService {
    NutritionEstimateResponse getEstimateForRecipe(Recipes recipe);

    void deleteEstimateForRecipe(Integer recipeId);
}
