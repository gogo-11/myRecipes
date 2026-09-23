package com.myrecipe.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Recipes;
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

        when(recipesRepository.findAllPublicRecipesOrderByIdDesc(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Recipes> result = service.getPublicRecipesPage(0, 6);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipesRepository).findAllPublicRecipesOrderByIdDesc(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(6);
        assertThat(result.getContent()).containsExactly(firstRecipe, secondRecipe);
        assertThat(result.getContent()).allMatch(recipe -> !recipe.getIsPrivate());
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
