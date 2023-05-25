package com.myrecipe.exceptions;

public class ImageFormatException extends RuntimeException{
    public ImageFormatException() {
    }

    public ImageFormatException(String message) {
        super(message);
    }
}
