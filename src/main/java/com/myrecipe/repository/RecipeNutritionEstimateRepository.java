package com.myrecipe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myrecipe.entities.RecipeNutritionEstimate;

@Repository
public interface RecipeNutritionEstimateRepository extends JpaRepository<RecipeNutritionEstimate, Integer> {
    Optional<RecipeNutritionEstimate> findByRecipeId(Integer recipeId);

    void deleteByRecipeId(Integer recipeId);
}
