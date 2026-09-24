package com.myrecipe.exceptions;

public class InvalidEmailConfirmationTokenException extends RuntimeException {
    public InvalidEmailConfirmationTokenException(String message) {
        super(message);
    }
}
