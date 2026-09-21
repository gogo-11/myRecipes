package com.myrecipe.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myrecipe.entities.RecipeNutritionEstimate;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.requests.NutritionEstimateRequest;
import com.myrecipe.entities.responses.NutritionEstimateResponse;
import com.myrecipe.exceptions.NutritionEstimateException;
import com.myrecipe.repository.RecipeNutritionEstimateRepository;

@Service
public class MyNutritionService implements NutritionService {
    @Autowired
    private RecipeNutritionEstimateRepository nutritionRepository;

    @Autowired
    private NutritionAiClient nutritionAiClient;

    @Override
    @Transactional
    public NutritionEstimateResponse getEstimateForRecipe(Recipes recipe) {
        if (recipe == null || recipe.getId() == null) {
            return null;
        }

        NutritionEstimateResponse cachedEstimate = nutritionRepository.findByRecipeId(recipe.getId())
                .map(this::toResponse)
                .orElse(null);
        if (cachedEstimate != null) {
            System.out.println("Nutrition estimate cache hit for recipe id " + recipe.getId());
            return cachedEstimate;
        }

        try {
            System.out.println("Nutrition estimate cache miss for recipe id " + recipe.getId() + ". Calling AI service.");
            NutritionEstimateResponse estimate = nutritionAiClient.estimate(toRequest(recipe));
            saveEstimate(recipe, estimate);
            System.out.println("Nutrition estimate saved for recipe id " + recipe.getId());
            return estimate;
        } catch (NutritionEstimateException e) {
            System.out.println("Nutrition estimate unavailable for recipe id "
                    + recipe.getId() + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteEstimateForRecipe(Integer recipeId) {
        if (recipeId != null) {
            nutritionRepository.deleteByRecipeId(recipeId);
        }
    }

    private NutritionEstimateResponse toResponse(RecipeNutritionEstimate estimate) {
        return new NutritionEstimateResponse(
                estimate.getCalories(),
                estimate.getProteinGrams(),
                estimate.getCarbohydratesGrams(),
                estimate.getFatGrams(),
                null,
                estimate.getEstimatedAt());
    }

    private NutritionEstimateRequest toRequest(Recipes recipe) {
        return new NutritionEstimateRequest(
                recipe.getRecipeName(),
                recipe.getProducts(),
                recipe.getPortions(),
                recipe.getCookingSteps());
    }

    private void saveEstimate(Recipes recipe, NutritionEstimateResponse response) {
        RecipeNutritionEstimate estimate = new RecipeNutritionEstimate();
        estimate.setRecipe(recipe);
        estimate.setCalories(response.getCalories());
        estimate.setProteinGrams(response.getProteinGrams());
        estimate.setCarbohydratesGrams(response.getCarbohydratesGrams());
        estimate.setFatGrams(response.getFatGrams());
        estimate.setModel(nutritionAiClient.getModelName());
        estimate.setEstimatedAt(response.getEstimatedAt());
        nutritionRepository.save(estimate);
    }
}
