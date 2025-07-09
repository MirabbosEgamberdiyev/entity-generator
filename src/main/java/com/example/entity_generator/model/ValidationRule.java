package com.example.entity_generator.model;

import java.util.Map;

public class ValidationRule {
    private String type; // NotNull, NotBlank, Size, Pattern, Min, Max, Email, etc.
    private Map<String, Object> parameters;
    private String message;
    private String[] groups;

    // Constructors
    public ValidationRule() {}

    public ValidationRule(String type) {
        this.type = type;
    }

    public ValidationRule(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    // Helper methods for common validation types
    public static ValidationRule notNull() {
        return new ValidationRule("NotNull");
    }

    public static ValidationRule notBlank() {
        return new ValidationRule("NotBlank");
    }

    public static ValidationRule size(int min, int max) {
        ValidationRule rule = new ValidationRule("Size");
        rule.parameters = Map.of("min", min, "max", max);
        return rule;
    }

    public static ValidationRule pattern(String regex) {
        ValidationRule rule = new ValidationRule("Pattern");
        rule.parameters = Map.of("regexp", regex);
        return rule;
    }

    public static ValidationRule min(long value) {
        ValidationRule rule = new ValidationRule("Min");
        rule.parameters = Map.of("value", value);
        return rule;
    }

    public static ValidationRule max(long value) {
        ValidationRule rule = new ValidationRule("Max");
        rule.parameters = Map.of("value", value);
        return rule;
    }

    public static ValidationRule email() {
        return new ValidationRule("Email");
    }

    public static ValidationRule positive() {
        return new ValidationRule("Positive");
    }

    public static ValidationRule negative() {
        return new ValidationRule("Negative");
    }

    public static ValidationRule digits(int integer, int fraction) {
        ValidationRule rule = new ValidationRule("Digits");
        rule.parameters = Map.of("integer", integer, "fraction", fraction);
        return rule;
    }

    public static ValidationRule decimalMin(String value, boolean inclusive) {
        ValidationRule rule = new ValidationRule("DecimalMin");
        rule.parameters = Map.of("value", value, "inclusive", inclusive);
        return rule;
    }

    public static ValidationRule decimalMax(String value, boolean inclusive) {
        ValidationRule rule = new ValidationRule("DecimalMax");
        rule.parameters = Map.of("value", value, "inclusive", inclusive);
        return rule;
    }

    public static ValidationRule future() {
        return new ValidationRule("Future");
    }

    public static ValidationRule past() {
        return new ValidationRule("Past");
    }

    public static ValidationRule futureOrPresent() {
        return new ValidationRule("FutureOrPresent");
    }

    public static ValidationRule pastOrPresent() {
        return new ValidationRule("PastOrPresent");
    }
}