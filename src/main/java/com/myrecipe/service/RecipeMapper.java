package com.myrecipe.service;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.responses.RecipeSummaryResponse;

@Component
public class RecipeMapper {
    public RecipeSummaryResponse toSummaryResponse(Recipes recipe) {
        return new RecipeSummaryResponse(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getPortions(),
                recipe.getCookingTime(),
                recipe.getCategory(),
                "/recipes/image/" + recipe.getId());
    }
}
