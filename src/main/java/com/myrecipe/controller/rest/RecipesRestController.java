package com.myrecipe.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.responses.RecipesResponse;
import com.myrecipe.service.RecipesService;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.requests.UsersRequest;


@RestController
@RequestMapping("/recipes")
public class RecipesRestController {

    @Autowired
    RecipesService recipesService;

    /**
     *
     * @param id ID of the wanted recipe
     * @return the recipe wanted and HTTP status code for successful operation
     */
    @GetMapping("/{id}")
    public ResponseEntity<Recipes> getRecipeById (@PathVariable("id") Integer id) {
        Recipes recipe = recipesService.getById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    /**
     *
     * @return all the recipes
     */
    @GetMapping("/all")
    public List<Recipes> getAllPublicRecipes() {
        List<Recipes> allRecipes = recipesService.getAllPublicRecipes();
        return allRecipes;
    }

    @GetMapping("/recipe-name")
    public ResponseEntity<Recipes> getRecipeByName (@RequestBody RecipesRequest request) {
        Recipes recipeByName = recipesService.getByName(request);
        return new ResponseEntity<>(recipeByName, HttpStatus.OK);
    }

    @GetMapping("/recipe-category")
    public List<Recipes> getByCategory (@RequestBody RecipesRequest request) {
        List<Recipes> recipeByCat = recipesService.getByCategory(request);
        return recipeByCat;
    }

    @PostMapping("/allUserPrivate")
    public List<Recipes> getAllPrivateRecipes(@RequestBody UsersRequest request) {
        List<Recipes> userPrivateRecipes = recipesService.getUsersAllPrivateRecipes(request);
        return userPrivateRecipes;
    }

    /**
     * Creating new recipe
     *
     * @param recipe data entered by the user
     * @return Response for newly created recipe and HTTP status code for successful creation
     */
    @PostMapping()
    public ResponseEntity<RecipesResponse> addNewRecipe(@RequestBody RecipesRequest recipe) {
        Integer userId = recipe.getUserId();
        RecipesResponse newRecipe = recipesService.createRecipe(recipe, userId);
        return new ResponseEntity<>(newRecipe, HttpStatus.CREATED);
    }

    /**
     *
     * @param recipeId id of the recipe which is to be deleted
     * @return confirmation for successful removal of the recipe and HTTP status code for successful operation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecipe (@PathVariable("id") Integer recipeId) {
        recipesService.deleteRecipe(recipeId);
        return new ResponseEntity<>("Recipe deleted successfully", HttpStatus.OK);
    }
}
