package com.myrecipe.entities.responses;

import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldValidationErrorResponse {
    private String message;
    private Map<String, String> fieldErrors;
    private HttpStatus httpStatus;
}
