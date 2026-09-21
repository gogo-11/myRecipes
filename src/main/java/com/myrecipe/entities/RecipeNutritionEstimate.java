package com.myrecipe.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipe_nutrition_estimates")
public class RecipeNutritionEstimate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nutrition_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false, unique = true)
    private Recipes recipe;

    @Column(nullable = false)
    private BigDecimal calories;

    @Column(name = "protein_grams", nullable = false)
    private BigDecimal proteinGrams;

    @Column(name = "carbohydrates_grams", nullable = false)
    private BigDecimal carbohydratesGrams;

    @Column(name = "fat_grams", nullable = false)
    private BigDecimal fatGrams;

    @Column(name = "ai_model")
    private String model;

    @Column(name = "notes")
    private String notes;

    @Column(name = "estimated_at", nullable = false)
    private LocalDateTime estimatedAt;
}
