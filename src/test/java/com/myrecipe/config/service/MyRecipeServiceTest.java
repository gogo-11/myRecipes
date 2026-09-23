package com.myrecipe.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Recipes;
import com.myrecipe.exceptions.InvalidCategoryException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.RecipesRepository;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.service.MyRecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class MyRecipeServiceTest {
    @Mock
    private RecipesRepository recipesRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void getPublicRecipesPageUsesPublicOnlyRepositoryQuery() {
        MyRecipeService service = new MyRecipeService(recipesRepository, usersRepository, passwordEncoder);
        Recipes firstRecipe = recipe(3, false);
        Recipes secondRecipe = recipe(2, false);
        Page<Recipes> expectedPage = new PageImpl<>(Arrays.asList(firstRecipe, secondRecipe));

        when(recipesRepository.findPublicRecipes(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Recipes> result = service.getPublicRecipesPage(0, 6);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipesRepository).findPublicRecipes(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(6);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
        assertThat(result.getContent()).containsExactly(firstRecipe, secondRecipe);
        assertThat(result.getContent()).allMatch(recipe -> !recipe.getIsPrivate());
    }

    @Test
    public void getPublicRecipesPageTrimsKeywordAndNormalizesCategory() {
        MyRecipeService service = new MyRecipeService(recipesRepository, usersRepository, passwordEncoder);
        Page<Recipes> expectedPage = new PageImpl<>(Arrays.asList(recipe(4, false)));

        when(recipesRepository.findPublicRecipes(
                org.mockito.ArgumentMatchers.eq("Chicken"),
                org.mockito.ArgumentMatchers.eq(Categories.MEAT),
                any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Recipes> result = service.getPublicRecipesPage("  Chicken  ", " meat ", 0, 6);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipesRepository).findPublicRecipes(
                org.mockito.ArgumentMatchers.eq("Chicken"),
                org.mockito.ArgumentMatchers.eq(Categories.MEAT),
                pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    public void getPublicRecipesPageTreatsBlankFiltersAsNoFilters() {
        MyRecipeService service = new MyRecipeService(recipesRepository, usersRepository, passwordEncoder);
        Page<Recipes> expectedPage = new PageImpl<>(Arrays.asList(recipe(6, false)));

        when(recipesRepository.findPublicRecipes(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Recipes> result = service.getPublicRecipesPage("   ", "   ", 0, 6);

        verify(recipesRepository).findPublicRecipes(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                any(Pageable.class));
        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    public void getPublicRecipesPageRejectsInvalidCategory() {
        MyRecipeService service = new MyRecipeService(recipesRepository, usersRepository, passwordEncoder);

        assertThatThrownBy(() -> service.getPublicRecipesPage(null, "not-a-category", 0, 6))
                .isInstanceOf(InvalidCategoryException.class)
                .hasMessage("Wrong category");
    }

    @Test
    public void getPublicRecipeByIdReturnsPublicRecipe() {
        MyRecipeService service = new MyRecipeService(recipesRepository, usersRepository, passwordEncoder);
        Recipes recipe = recipe(5, false);
        when(recipesRepository.findPublicRecipeById(5)).thenReturn(Optional.of(recipe));

        Recipes result = service.getPublicRecipeById(5);

        assertThat(result).isEqualTo(recipe);
        verify(recipesRepository).findPublicRecipeById(5);
    }

    @Test
    public void getPublicRecipeByIdTreatsPrivateRecipeAsNotFound() {
        MyRecipeService service = new MyRecipeService(recipesRepository, usersRepository, passwordEncoder);
        when(recipesRepository.findPublicRecipeById(8)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublicRecipeById(8))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Recipe with the specified ID does not exist!");

        verify(recipesRepository).findPublicRecipeById(8);
    }

    private Recipes recipe(Integer id, Boolean isPrivate) {
        Recipes recipe = new Recipes();
        recipe.setId(id);
        recipe.setRecipeName("Recipe " + id);
        recipe.setPortions(4);
        recipe.setCookingTime(30);
        recipe.setCategory(Categories.MEATLESS);
        recipe.setIsPrivate(isPrivate);
        return recipe;
    }
}
