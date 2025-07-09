package com.example.entity_generator.model;

import java.util.List;
import java.util.Map;

public  class GenerationResult {
    private boolean success;
    private String message;
    private List<String> generatedFiles;
    private Map<String, String> errors;

    public static GenerationResult success(String message, List<String> files) {
        GenerationResult result = new GenerationResult();
        result.success = true;
        result.message = message;
        result.generatedFiles = files;
        return result;
    }

    public static GenerationResult error(String message) {
        GenerationResult result = new GenerationResult();
        result.success = false;
        result.message = message;
        return result;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<String> getGeneratedFiles() { return generatedFiles; }
    public void setGeneratedFiles(List<String> generatedFiles) { this.generatedFiles = generatedFiles; }
    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
}
