package com.library.exception;

public class ConfigurationFileNotFoundException extends RuntimeException {
    public ConfigurationFileNotFoundException(String message) {
        super(message);
    }
}