package com.myrecipe.exceptions;

public class InvalidLoginDataException extends RuntimeException {
    public InvalidLoginDataException() {
    }

    public InvalidLoginDataException(String message) {
        super(message);
    }
}
