package com.myrecipe.exceptions.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.myrecipe.entities.responses.MyApiErrorResponse;
import com.myrecipe.exceptions.DuplicateRecordFoundException;
import com.myrecipe.exceptions.ImageFormatException;
import com.myrecipe.exceptions.InvalidCategoryException;
import com.myrecipe.exceptions.InvalidLoginDataException;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.NutritionEstimateException;
import com.myrecipe.exceptions.RecordNotFoundException;

@RestControllerAdvice
public class MyRestExceptionHandler {
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<MyApiErrorResponse> handleRecordNotFoundException(RecordNotFoundException e) {
        return buildResponse(
                "Record was not found",
                e.getMessage(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidUserRequestException.class)
    public ResponseEntity<MyApiErrorResponse> handleInvalidUserRequestException(InvalidUserRequestException e) {
        return buildResponse(
                "Invalid request",
                e.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MyApiErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        return buildResponse(
                "Invalid request parameter",
                "Invalid value for parameter: " + e.getName(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<MyApiErrorResponse> handleSecurityException(SecurityException e) {
        return buildResponse(
                "Forbidden",
                e.getMessage(),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidLoginDataException.class)
    public ResponseEntity<MyApiErrorResponse> handleInvalidLoginDataException(InvalidLoginDataException e) {
        return buildResponse(
                "Invalid login data",
                e.getMessage(),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateRecordFoundException.class)
    public ResponseEntity<MyApiErrorResponse> handleDuplicateRecordFoundException(DuplicateRecordFoundException e) {
        return buildResponse(
                "Duplicate record",
                e.getMessage(),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<MyApiErrorResponse> handleInvalidCategoryException(InvalidCategoryException e) {
        return buildResponse(
                "Invalid category",
                e.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageFormatException.class)
    public ResponseEntity<MyApiErrorResponse> handleImageFormatException(ImageFormatException e) {
        return buildResponse(
                "Invalid image",
                e.getMessage(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(NutritionEstimateException.class)
    public ResponseEntity<MyApiErrorResponse> handleNutritionEstimateException(NutritionEstimateException e) {
        return buildResponse(
                "Nutrition estimate unavailable",
                e.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MyApiErrorResponse> handleException(Exception e) {
        return buildResponse(
                "Unexpected server error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<MyApiErrorResponse> buildResponse(String message, String details, HttpStatus status) {
        MyApiErrorResponse response = new MyApiErrorResponse(message, details, status);
        return new ResponseEntity<>(response, status);
    }
}
