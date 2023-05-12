package com.myrecipe.entities.responses;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyApiErrorResponse {
    private String message;
    private String details;
    private HttpStatus httpStatus;

    public MyApiErrorResponse(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public MyApiErrorResponse(String message, String details, HttpStatus httpStatus) {
        this.message = message;
        this.details = details;
        this.httpStatus = httpStatus;
    }
}
