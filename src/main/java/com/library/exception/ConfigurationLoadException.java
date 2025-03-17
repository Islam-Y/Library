package com.library.exception;

public class ConfigurationLoadException extends RuntimeException {
    public ConfigurationLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}