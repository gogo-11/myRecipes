package com.myrecipe.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.responses.NutritionEstimateResponse;
import com.myrecipe.service.NutritionService;
import com.myrecipe.service.RecipesService;

@RestController
@RequestMapping("/api/v1/recipes")
public class NutritionRestController {
    @Autowired
    private RecipesService recipesService;

    @Autowired
    private NutritionService nutritionService;

    @GetMapping("/{id}/nutrition")
    public ResponseEntity<NutritionEstimateResponse> getRecipeNutrition(@PathVariable("id") Integer id) {
        Recipes recipe = recipesService.getById(id);
        NutritionEstimateResponse nutritionEstimate = nutritionService.getCachedEstimateForRecipe(recipe);
        if (nutritionEstimate == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(nutritionEstimate, HttpStatus.OK);
    }

    @PostMapping("/{id}/nutrition")
    public ResponseEntity<NutritionEstimateResponse> generateRecipeNutrition(@PathVariable("id") Integer id) {
        Recipes recipe = recipesService.getById(id);
        NutritionEstimateResponse nutritionEstimate = nutritionService.generateEstimateForRecipe(recipe);
        if (nutritionEstimate == null) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        return new ResponseEntity<>(nutritionEstimate, HttpStatus.CREATED);
    }
}
