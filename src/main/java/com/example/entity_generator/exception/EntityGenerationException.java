package com.example.entity_generator.exception;


public class EntityGenerationException extends RuntimeException {
    public EntityGenerationException(String message) {
        super(message);
    }

    public EntityGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}