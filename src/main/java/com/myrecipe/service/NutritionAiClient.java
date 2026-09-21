package com.myrecipe.service;

import com.myrecipe.entities.requests.NutritionEstimateRequest;
import com.myrecipe.entities.responses.NutritionEstimateResponse;

public interface NutritionAiClient {
    NutritionEstimateResponse estimate(NutritionEstimateRequest request);

    String getModelName();
}
