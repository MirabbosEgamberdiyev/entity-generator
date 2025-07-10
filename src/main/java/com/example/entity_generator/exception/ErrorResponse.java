package com.example.entity_generator.exception;

public class ErrorResponse {
    private int status;
    private String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
}
