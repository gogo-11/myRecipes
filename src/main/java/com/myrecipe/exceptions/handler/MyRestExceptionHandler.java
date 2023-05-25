package com.myrecipe.exceptions.handler;

import com.myrecipe.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.myrecipe.entities.responses.MyApiErrorResponse;

@ControllerAdvice
public class MyRestExceptionHandler {

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<MyApiErrorResponse> handleRecordNotFoundException (RecordNotFoundException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                "Record with the specified value was not found.",
                e.getMessage() + " Check the entered data and try again.",
                HttpStatus.NOT_FOUND);

        return new ResponseEntity<> (response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidUserRequestException.class)
    public ResponseEntity<MyApiErrorResponse> handleInvalidUserRequestException (InvalidUserRequestException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Your request is invalid. Please make sure you entered the correct data and try again.",
                HttpStatus.BAD_REQUEST);

        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<MyApiErrorResponse> handleSecurityException (SecurityException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                "Wrong email or password for accessing private recipes submitted.",
                e.getMessage(),
                HttpStatus.FORBIDDEN);

        return new ResponseEntity<> (response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidLoginDataException.class)
    public ResponseEntity<MyApiErrorResponse> handleInvalidLoginDataException (InvalidLoginDataException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "The email or password you provided are either wrong or invalid. Please try again.",
                HttpStatus.FORBIDDEN);

        return new ResponseEntity<> (response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateRecordFoundException.class)
    public ResponseEntity<MyApiErrorResponse> handleDuplicateRecordFoundException (DuplicateRecordFoundException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                "The value provided is duplicate. Please enter another value!",
                e.getMessage(),
                HttpStatus.BAD_REQUEST);

        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<MyApiErrorResponse> handleInvalidCategoryException (InvalidCategoryException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Please check the category field and try again!",
                HttpStatus.BAD_REQUEST);

        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ImageFormatException.class)
    public ResponseEntity<MyApiErrorResponse> handleImageFormatException (ImageFormatException e) {
        MyApiErrorResponse response = new MyApiErrorResponse(
                e.getMessage(),
                "Please ensure that you uploaded a valid image!",
                HttpStatus.BAD_REQUEST);

        return new ResponseEntity<> (response, HttpStatus.BAD_REQUEST);
    }
}
