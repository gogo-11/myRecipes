package com.myrecipe.exceptions;

public class InvalidUserRequestException extends RuntimeException {
    public InvalidUserRequestException() {
    }

    public InvalidUserRequestException(String message) {
        super(message);
    }
}
