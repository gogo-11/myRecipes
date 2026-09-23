package com.myrecipe.config.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Comments;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.Users;
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
        assertThat(response.getImageUrl()).isEqualTo("/recipes/image/7");
    }
}
