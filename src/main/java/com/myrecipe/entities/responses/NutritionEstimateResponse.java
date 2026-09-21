package com.myrecipe.entities.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionEstimateResponse {
    private BigDecimal calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbohydratesGrams;
    private BigDecimal fatGrams;
    private String notes;
    private LocalDateTime estimatedAt;
}
