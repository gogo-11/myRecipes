package com.myrecipe.exceptions;

public class NutritionEstimateException extends RuntimeException {
    public NutritionEstimateException(String message) {
        super(message);
    }

    public NutritionEstimateException(String message, Throwable cause) {
        super(message, cause);
    }
}
