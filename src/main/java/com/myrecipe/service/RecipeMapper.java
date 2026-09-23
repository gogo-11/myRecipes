package com.myrecipe.service;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.responses.AuthorSummaryResponse;
import com.myrecipe.entities.responses.RecipeDetailsResponse;
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
                imageUrl(recipe.getId()));
    }

    public RecipeDetailsResponse toDetailsResponse(Recipes recipe) {
        return new RecipeDetailsResponse(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getProducts(),
                recipe.getPortions(),
                recipe.getCookingTime(),
                recipe.getCookingSteps(),
                recipe.getCategory(),
                imageUrl(recipe.getId()),
                toAuthorSummaryResponse(recipe.getUser()));
    }

    private AuthorSummaryResponse toAuthorSummaryResponse(Users author) {
        return new AuthorSummaryResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName());
    }

    private String imageUrl(Integer recipeId) {
        return "/api/v1/recipes/" + recipeId + "/image";
    }
}
