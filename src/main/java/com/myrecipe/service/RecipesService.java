package com.myrecipe.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.RecipesResponse;
import com.myrecipe.entities.Categories;

@Component
public interface RecipesService {
    RecipesResponse createRecipe(RecipesRequest recipesRequest, Integer userId);
    Recipes getById(Integer id);
    Recipes getByName(RecipesRequest request);
    List<Recipes> getByCategory (RecipesRequest request);
    List<Recipes> getAllPublicRecipes();
    List<Recipes> getUsersAllPrivateRecipes(UsersRequest usersRequest);
    void deleteRecipe (Integer recipeId);
}
