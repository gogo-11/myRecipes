package com.myrecipe.exceptions;

public class UserNotFoundException extends Throwable {
    public UserNotFoundException(String email) {
        super(email);
    }
}
