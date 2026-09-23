package com.myrecipe.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.RecipeDetailsResponse;
import com.myrecipe.entities.responses.RecipePageResponse;
import com.myrecipe.entities.responses.RecipeSummaryResponse;
import com.myrecipe.entities.responses.RecipesResponse;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.service.NutritionService;
import com.myrecipe.service.RecipeMapper;
import com.myrecipe.service.RecipesService;


@RestController
@RequestMapping("/api/v1/recipes")
public class RecipesRestController {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 6;
    private static final int MAX_SIZE = 50;

    private final RecipesService recipesService;
    private final NutritionService nutritionService;
    private final RecipeMapper recipeMapper;

    public RecipesRestController(RecipesService recipesService,
                                 NutritionService nutritionService,
                                 RecipeMapper recipeMapper) {
        this.recipesService = recipesService;
        this.nutritionService = nutritionService;
        this.recipeMapper = recipeMapper;
    }

    @GetMapping
    public ResponseEntity<RecipePageResponse> getPublicRecipes(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) Integer page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) Integer size) {
        validatePagination(page, size);

        Page<Recipes> recipesPage = recipesService.getPublicRecipesPage(page, size);
        List<RecipeSummaryResponse> content = recipesPage.getContent().stream()
                .map(recipeMapper::toSummaryResponse)
                .collect(Collectors.toList());

        RecipePageResponse response = new RecipePageResponse(
                content,
                recipesPage.getNumber(),
                recipesPage.getSize(),
                recipesPage.getTotalElements(),
                recipesPage.getTotalPages(),
                recipesPage.isFirst(),
                recipesPage.isLast());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     *
     * @param id ID of the wanted recipe
     * @return the recipe wanted and HTTP status code for successful operation
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailsResponse> getRecipeById (@PathVariable("id") Integer id) {
        Recipes recipe = recipesService.getPublicRecipeById(id);
        return new ResponseEntity<>(recipeMapper.toDetailsResponse(recipe), HttpStatus.OK);
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
        nutritionService.deleteEstimateForRecipe(recipeId);
        recipesService.deleteRecipe(recipeId);
        return new ResponseEntity<>("Recipe deleted successfully", HttpStatus.OK);
    }

    private void validatePagination(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new InvalidUserRequestException("Page must be greater than or equal to 0");
        }
        if (size == null || size < 1) {
            throw new InvalidUserRequestException("Size must be greater than or equal to 1");
        }
        if (size > MAX_SIZE) {
            throw new InvalidUserRequestException("Size must be less than or equal to " + MAX_SIZE);
        }
    }
}
