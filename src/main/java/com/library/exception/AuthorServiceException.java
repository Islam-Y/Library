package com.library.exception;

public class AuthorServiceException extends RuntimeException {
    public AuthorServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}