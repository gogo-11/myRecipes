package com.myrecipe.config.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Comments;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.responses.RecipeDetailsResponse;
import com.myrecipe.entities.responses.RecipeSummaryResponse;
import com.myrecipe.service.RecipeMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class RecipeMapperTest {
    private final RecipeMapper mapper = new RecipeMapper();

    @Test
    public void toSummaryResponseMapsOnlySummaryFields() {
        Recipes recipe = new Recipes();
        recipe.setId(7);
        recipe.setRecipeName("Палачинки");
        recipe.setPortions(2);
        recipe.setCookingTime(15);
        recipe.setCategory(Categories.DESSERTS);
        recipe.setImage(new byte[] {1, 2, 3});
        recipe.setUser(new Users());
        recipe.setComments(Collections.singletonList(new Comments()));

        RecipeSummaryResponse response = mapper.toSummaryResponse(recipe);

        assertThat(response.getId()).isEqualTo(7);
        assertThat(response.getRecipeName()).isEqualTo("Палачинки");
        assertThat(response.getPortions()).isEqualTo(2);
        assertThat(response.getCookingTime()).isEqualTo(15);
        assertThat(response.getCategory()).isEqualTo(Categories.DESSERTS);
        assertThat(response.getImageUrl()).isEqualTo("/api/v1/recipes/7/image");
    }

    @Test
    public void toDetailsResponseMapsAllowedFieldsAndAuthorSummary() {
        Users author = new Users();
        author.setId(3);
        author.setFirstName("Мария");
        author.setLastName("Иванова");
        author.setEmail("secret@mail.com");
        author.setPassword("secret-password");

        Recipes recipe = new Recipes();
        recipe.setId(9);
        recipe.setRecipeName("Супа");
        recipe.setProducts("моркови\nкартофи");
        recipe.setPortions(4);
        recipe.setCookingTime(40);
        recipe.setCookingSteps("Нарежи\nСвари");
        recipe.setCategory(Categories.SOUPS);
        recipe.setImage(new byte[] {1, 2, 3});
        recipe.setUser(author);
        recipe.setComments(Collections.singletonList(new Comments()));

        RecipeDetailsResponse response = mapper.toDetailsResponse(recipe);

        assertThat(response.getId()).isEqualTo(9);
        assertThat(response.getRecipeName()).isEqualTo("Супа");
        assertThat(response.getProducts()).isEqualTo("моркови\nкартофи");
        assertThat(response.getPortions()).isEqualTo(4);
        assertThat(response.getCookingTime()).isEqualTo(40);
        assertThat(response.getCookingSteps()).isEqualTo("Нарежи\nСвари");
        assertThat(response.getCategory()).isEqualTo(Categories.SOUPS);
        assertThat(response.getImageUrl()).isEqualTo("/api/v1/recipes/9/image");
        assertThat(response.getAuthor().getId()).isEqualTo(3);
        assertThat(response.getAuthor().getFirstName()).isEqualTo("Мария");
        assertThat(response.getAuthor().getLastName()).isEqualTo("Иванова");
    }
}
