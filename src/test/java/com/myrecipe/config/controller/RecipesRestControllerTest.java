package com.myrecipe.config.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.myrecipe.entities.responses.RecipeSummaryResponse;
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

        when(recipesService.getPublicRecipesPage(0, 6)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(firstRecipe)).thenReturn(summary(firstRecipe));
        when(recipeMapper.toSummaryResponse(secondRecipe)).thenReturn(summary(secondRecipe));

        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].recipeName").value("Трета рецепта"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("/recipes/image/3"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(6))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(recipesService).getPublicRecipesPage(0, 6);
    }

    @Test
    public void getPublicRecipesReturnsEmptyPage() throws Exception {
        Page<Recipes> recipePage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 6),
                0);
        when(recipesService.getPublicRecipesPage(0, 6)).thenReturn(recipePage);

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

        when(recipesService.getPublicRecipesPage(1, 1)).thenReturn(recipePage);
        when(recipeMapper.toSummaryResponse(any(Recipes.class))).thenReturn(summary(recipe));

        mockMvc.perform(get("/api/v1/recipes").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));

        verify(recipesService).getPublicRecipesPage(eq(1), eq(1));
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
                "/recipes/image/" + recipe.getId());
    }
}
