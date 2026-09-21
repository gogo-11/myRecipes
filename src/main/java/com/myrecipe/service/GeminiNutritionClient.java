package com.myrecipe.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myrecipe.entities.requests.NutritionEstimateRequest;
import com.myrecipe.entities.responses.NutritionEstimateResponse;
import com.myrecipe.exceptions.NutritionEstimateException;

@Service
public class GeminiNutritionClient implements NutritionAiClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash}")
    private String model;

    @Value("${gemini.fallback-model:gemini-3.5-flash-lite}")
    private String fallbackModel;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Autowired
    public GeminiNutritionClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public NutritionEstimateResponse estimate(NutritionEstimateRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new NutritionEstimateException("Gemini API key is not configured");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildGeminiRequest(request));
            HttpResponse<String> response = sendRequest(requestBody, model);
            if (isRetryableStatus(response.statusCode()) && hasFallbackModel()) {
                System.out.println("Gemini model " + getModelName()
                        + " returned status " + response.statusCode()
                        + ". Retrying with " + normalizeModelName(fallbackModel));
                response = sendRequest(requestBody, fallbackModel);
            }

            validateResponse(response);

            return parseGeminiResponse(response.body());
        } catch (IOException e) {
            throw new NutritionEstimateException("Failed to process Gemini nutrition response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NutritionEstimateException("Gemini nutrition request was interrupted", e);
        }
    }

    @Override
    public String getModelName() {
        return normalizeModelName(model);
    }

    private Map<String, Object> buildGeminiRequest(NutritionEstimateRequest request) {
        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("text", buildPrompt(request));

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("parts", Arrays.asList(textPart));

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema", buildResponseSchema());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", Arrays.asList(content));
        body.put("generationConfig", generationConfig);
        return body;
    }

    private String buildPrompt(NutritionEstimateRequest request) {
        return "Estimate the nutrition values per one portion for this recipe. "
                + "Use the number of portions to divide the whole recipe totals. "
                + "Return only valid JSON matching the requested schema. "
                + "Use grams for protein, carbohydrates and fat. "
                + "Recipe name: " + request.getRecipeName() + "\n"
                + "Portions: " + request.getPortions() + "\n"
                + "Ingredients/products:\n" + request.getProducts() + "\n"
                + "Cooking steps:\n" + safeText(request.getCookingSteps());
    }

    private Map<String, Object> buildResponseSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("calories", numericSchema("Estimated kcal per portion"));
        properties.put("proteinGrams", numericSchema("Estimated protein grams per portion"));
        properties.put("carbohydratesGrams", numericSchema("Estimated carbohydrate grams per portion"));
        properties.put("fatGrams", numericSchema("Estimated fat grams per portion"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("calories", "proteinGrams", "carbohydratesGrams", "fatGrams"));
        return schema;
    }

    private Map<String, Object> numericSchema(String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "number");
        schema.put("description", description);
        return schema;
    }

    private HttpResponse<String> sendRequest(String requestBody, String modelName)
            throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(buildGenerateContentUrl(modelName)))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new NutritionEstimateException("Gemini API returned status "
                    + response.statusCode() + ": " + abbreviate(response.body()));
        }
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502
                || statusCode == 503 || statusCode == 504;
    }

    private boolean hasFallbackModel() {
        return fallbackModel != null && !fallbackModel.isBlank()
                && !normalizeModelName(fallbackModel).equals(normalizeModelName(model));
    }

    private NutritionEstimateResponse parseGeminiResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new NutritionEstimateException("Gemini API returned no nutrition candidate");
        }

        String nutritionJson = stripCodeFence(parts.get(0).path("text").asText());
        JsonNode nutritionNode = objectMapper.readTree(nutritionJson);

        return new NutritionEstimateResponse(
                requiredDecimal(nutritionNode, "calories"),
                requiredDecimal(nutritionNode, "proteinGrams"),
                requiredDecimal(nutritionNode, "carbohydratesGrams"),
                requiredDecimal(nutritionNode, "fatGrams"),
                "AI-generated estimate. Values can vary by ingredient brand and exact quantities.",
                LocalDateTime.now());
    }

    private BigDecimal requiredDecimal(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || !value.isNumber()) {
            throw new NutritionEstimateException("Gemini response is missing numeric field: " + fieldName);
        }
        return value.decimalValue();
    }

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private String buildGenerateContentUrl() {
        return buildGenerateContentUrl(model);
    }

    private String buildGenerateContentUrl(String modelName) {
        return trimTrailingSlash(baseUrl) + "/" + normalizeModelName(modelName) + ":generateContent";
    }

    private String normalizeModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return "models/gemini-3.5-flash";
        }
        String trimmed = modelName.trim();
        return trimmed.startsWith("models/") ? trimmed : "models/" + trimmed;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
