package com.myrecipe.entities.responses;

import com.myrecipe.entities.Categories;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSummaryResponse {
    private Integer id;
    private String recipeName;
    private Integer portions;
    private Integer cookingTime;
    private Categories category;
    private String imageUrl;
}
