package com.myrecipe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.RecipesResponse;

@Component
public interface RecipesService {
    RecipesResponse createRecipe(RecipesRequest recipesRequest, Integer userId);
    Recipes getById(Integer id);
    Recipes getByName(RecipesRequest request);
    List<Recipes> getByKeyword(String keyword);
    List<Recipes> getByCategory (RecipesRequest request);
    List<Recipes> getRandomThreeByCategory (Integer recipeId, RecipesRequest request);
    List<Recipes> getAllPublicRecipes();
    List<Recipes> getLastTenPublicRecipes();
    List<Recipes> getUsersAllPrivateRecipes(UsersRequest usersRequest);
    List<Recipes> getUsersAllPublicRecipes(Integer id);
    Page<Recipes> getPage(int pageNo);
    Optional<Recipes> recipeUpdate(Integer id, RecipesRequest request);
    void deleteRecipe (Integer recipeId);
}
