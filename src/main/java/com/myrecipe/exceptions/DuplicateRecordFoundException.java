package com.myrecipe.exceptions;

public class DuplicateRecordFoundException extends RuntimeException {
    public DuplicateRecordFoundException() {
    }

    public DuplicateRecordFoundException(String message) {
        super(message);
    }
}
