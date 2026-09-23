package com.myrecipe.config.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import com.myrecipe.controller.rest.RecipesRestController;
import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.responses.AuthorSummaryResponse;
import com.myrecipe.entities.responses.RecipeDetailsResponse;
import com.myrecipe.entities.responses.RecipeSummaryResponse;
import com.myrecipe.exceptions.InvalidCategoryException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.service.NutritionService;
import com.myrecipe.service.RecipeMapper;
import com.myrecipe.service.RecipesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecipesRestController.class)
public class RecipesRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipesService recipesService;

    @MockBean
    private NutritionService nutritionService;

    @MockBean
    private RecipeMapper recipeMapper;

    @MockBean(name = "userDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Test
    public void getPublicRecipesReturnsPopulatedPage() throws Exception {
        Recipes firstRecipe = recipe(3, "Трета рецепта", false);
        Recipes secondRecipe = recipe(2, "Втора рецепта", false);
        Page<Recipes> recipePage = new PageImpl<>(
                Arrays.asList(firstRecipe, secondRecipe),
                PageRequest.of(0, 6),
                2);

        when(recipesService.getPublicRecipesPage(null, null, 0, 6)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(firstRecipe)).thenReturn(summary(firstRecipe));
        when(recipeMapper.toSummaryResponse(secondRecipe)).thenReturn(summary(secondRecipe));

        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].recipeName").value("Трета рецепта"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("/api/v1/recipes/3/image"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(6))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(recipesService).getPublicRecipesPage(null, null, 0, 6);
    }

    @Test
    public void getPublicRecipesReturnsEmptyPage() throws Exception {
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 6),
                0);
        when(recipesService.getPublicRecipesPage(null, null, 0, 6)).thenReturn(recipePage);

        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    public void getPublicRecipesRejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/recipes").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.details").value("Page must be greater than or equal to 0"));
    }

    @Test
    public void getPublicRecipesRejectsSizeBelowOne() throws Exception {
        mockMvc.perform(get("/api/v1/recipes").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.details").value("Size must be greater than or equal to 1"));
    }

    @Test
    public void getPublicRecipesRejectsSizeAboveFifty() throws Exception {
        mockMvc.perform(get("/api/v1/recipes").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.details").value("Size must be less than or equal to 50"));
    }

    @Test
    public void getPublicRecipesUsesStablePaginationMetadata() throws Exception {
        Recipes recipe = recipe(10, "Рецепта", false);
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.singletonList(recipe),
                PageRequest.of(1, 1),
                3);

        when(recipesService.getPublicRecipesPage(null, null, 1, 1)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(any(Recipes.class))).thenReturn(summary(recipe));

        mockMvc.perform(get("/api/v1/recipes").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        verify(recipesService).getPublicRecipesPage(isNull(), isNull(), eq(1), eq(1));
    }

    @Test
    public void getPublicRecipesAcceptsKeywordFilter() throws Exception {
        Recipes recipe = recipe(11, "Chicken soup", false);
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.singletonList(recipe),
                PageRequest.of(0, 6),
                1);

        when(recipesService.getPublicRecipesPage(" chicken ", null, 0, 6)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(recipe)).thenReturn(summary(recipe));

        mockMvc.perform(get("/api/v1/recipes").param("keyword", " chicken "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].recipeName").value("Chicken soup"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("/api/v1/recipes/11/image"));

        verify(recipesService).getPublicRecipesPage(" chicken ", null, 0, 6);
    }

    @Test
    public void getPublicRecipesAcceptsCategoryFilter() throws Exception {
        Recipes recipe = recipe(13, "Tomato soup", false);
        recipe.setCategory(Categories.SOUPS);
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.singletonList(recipe),
                PageRequest.of(0, 6),
                1);

        when(recipesService.getPublicRecipesPage(null, "SOUPS", 0, 6)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(recipe)).thenReturn(summary(recipe));

        mockMvc.perform(get("/api/v1/recipes").param("category", "SOUPS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category").value("SOUPS"));

        verify(recipesService).getPublicRecipesPage(null, "SOUPS", 0, 6);
    }

    @Test
    public void getPublicRecipesAcceptsKeywordAndCategoryFilters() throws Exception {
        Recipes recipe = recipe(14, "Chicken salad", false);
        recipe.setCategory(Categories.SALADS);
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.singletonList(recipe),
                PageRequest.of(1, 2),
                3);

        when(recipesService.getPublicRecipesPage("Chicken", "SALADS", 1, 2)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(recipe)).thenReturn(summary(recipe));

        mockMvc.perform(get("/api/v1/recipes")
                        .param("keyword", "Chicken")
                        .param("category", "SALADS")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(recipesService).getPublicRecipesPage("Chicken", "SALADS", 1, 2);
    }

    @Test
    public void getPublicRecipesReturnsEmptyPageForNoMatches() throws Exception {
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 6),
                0);
        when(recipesService.getPublicRecipesPage("missing", null, 0, 6)).thenReturn(recipePage);

        mockMvc.perform(get("/api/v1/recipes").param("keyword", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(recipesService).getPublicRecipesPage("missing", null, 0, 6);
    }

    @Test
    public void getPublicRecipesReturnsBadRequestForInvalidCategory() throws Exception {
        when(recipesService.getPublicRecipesPage(null, "INVALID", 0, 6))
                .thenThrow(new InvalidCategoryException("Wrong category"));

        mockMvc.perform(get("/api/v1/recipes").param("category", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid category"))
                .andExpect(jsonPath("$.details").value("Wrong category"));
    }

    @Test
    public void getPublicRecipeDetailsReturnsDetailsDto() throws Exception {
        Recipes recipe = recipe(12, "Детайлна рецепта", false);
        recipe.setProducts("яйца\nсирене");
        recipe.setCookingSteps("Разбий\nИзпечи");
        Users author = author(5, "Иван", "Петров");
        recipe.setUser(author);

        when(recipesService.getPublicRecipeById(12)).thenReturn(recipe);
        when(recipeMapper.toDetailsResponse(recipe)).thenReturn(details(recipe));

        mockMvc.perform(get("/api/v1/recipes/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.recipeName").value("Детайлна рецепта"))
                .andExpect(jsonPath("$.products").value("яйца\nсирене"))
                .andExpect(jsonPath("$.cookingSteps").value("Разбий\nИзпечи"))
                .andExpect(jsonPath("$.imageUrl").value("/api/v1/recipes/12/image"))
                .andExpect(jsonPath("$.author.id").value(5))
                .andExpect(jsonPath("$.author.firstName").value("Иван"))
                .andExpect(jsonPath("$.author.lastName").value("Петров"))
                .andExpect(jsonPath("$.image").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.comments").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.emailConfirmationToken").doesNotExist())
                .andExpect(jsonPath("$.passwordResetToken").doesNotExist());

        verify(recipesService).getPublicRecipeById(12);
    }

    @Test
    public void getPublicRecipeDetailsReturnsNotFoundForMissingRecipe() throws Exception {
        when(recipesService.getPublicRecipeById(404))
                .thenThrow(new RecordNotFoundException("Recipe with the specified ID does not exist!"));

        mockMvc.perform(get("/api/v1/recipes/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Record was not found"))
                .andExpect(jsonPath("$.details").value("Recipe with the specified ID does not exist!"));

        verifyNoInteractions(recipeMapper);
    }

    @Test
    public void getPublicRecipeDetailsReturnsNotFoundForPrivateRecipe() throws Exception {
        when(recipesService.getPublicRecipeById(15))
                .thenThrow(new RecordNotFoundException("Recipe with the specified ID does not exist!"));

        mockMvc.perform(get("/api/v1/recipes/15"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Record was not found"))
                .andExpect(jsonPath("$.details").value("Recipe with the specified ID does not exist!"));

        verifyNoInteractions(recipeMapper);
    }

    private Recipes recipe(Integer id, String name, Boolean isPrivate) {
        Recipes recipe = new Recipes();
        recipe.setId(id);
        recipe.setRecipeName(name);
        recipe.setPortions(4);
        recipe.setCookingTime(30);
        recipe.setCategory(Categories.MEATLESS);
        recipe.setIsPrivate(isPrivate);
        return recipe;
    }

    private RecipeSummaryResponse summary(Recipes recipe) {
        return new RecipeSummaryResponse(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getPortions(),
                recipe.getCookingTime(),
                recipe.getCategory(),
                "/api/v1/recipes/" + recipe.getId() + "/image");
    }

    private RecipeDetailsResponse details(Recipes recipe) {
        return new RecipeDetailsResponse(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getProducts(),
                recipe.getPortions(),
                recipe.getCookingTime(),
                recipe.getCookingSteps(),
                recipe.getCategory(),
                "/api/v1/recipes/" + recipe.getId() + "/image",
                new AuthorSummaryResponse(
                        recipe.getUser().getId(),
                        recipe.getUser().getFirstName(),
                        recipe.getUser().getLastName()));
    }

    private Users author(Integer id, String firstName, String lastName) {
        Users user = new Users();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail("author" + id + "@mail.com");
        return user;
    }
}
