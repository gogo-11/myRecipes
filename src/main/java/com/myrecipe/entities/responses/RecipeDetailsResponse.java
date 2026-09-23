package com.myrecipe.entities.responses;

import com.myrecipe.entities.Categories;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailsResponse {
    private Integer id;
    private String recipeName;
    private String products;
    private Integer portions;
    private Integer cookingTime;
    private String cookingSteps;
    private Categories category;
    private String imageUrl;
    private AuthorSummaryResponse author;
}
