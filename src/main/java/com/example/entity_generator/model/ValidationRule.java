package com.example.entity_generator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Validation rule for a field")
public class ValidationRule {
    @Schema(description = "Type of validation rule", example = "NotNull")
    private String type;

    @Schema(description = "Parameters for the validation rule")
    private Map<String, Object> parameters;

    @Schema(description = "Custom error message for the validation")
    private String message;

    @Schema(description = "Validation groups")
    private String[] groups;

    public ValidationRule(String type) {
        this.type = type;
    }

    public ValidationRule(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    // Enum for validation types
    public enum ValidationType {
        NOT_NULL, NOT_BLANK, SIZE, PATTERN, MIN, MAX, EMAIL, POSITIVE, NEGATIVE, DIGITS,
        DECIMAL_MIN, DECIMAL_MAX, FUTURE, PAST, FUTURE_OR_PRESENT, PAST_OR_PRESENT
    }

    // Helper methods for common validation types
    public static ValidationRule notNull() {
        return new ValidationRule(ValidationType.NOT_NULL.name());
    }

    public static ValidationRule notBlank() {
        return new ValidationRule(ValidationType.NOT_BLANK.name());
    }

    public static ValidationRule size(int min, int max) {
        ValidationRule rule = new ValidationRule(ValidationType.SIZE.name());
        rule.parameters = Map.of("min", min, "max", max);
        return rule;
    }

    public static ValidationRule pattern(String regex) {
        ValidationRule rule = new ValidationRule(ValidationType.PATTERN.name());
        rule.parameters = Map.of("regexp", regex);
        return rule;
    }

    public static ValidationRule min(long value) {
        ValidationRule rule = new ValidationRule(ValidationType.MIN.name());
        rule.parameters = Map.of("value", value);
        return rule;
    }

    public static ValidationRule max(long value) {
        ValidationRule rule = new ValidationRule(ValidationType.MAX.name());
        rule.parameters = Map.of("value", value);
        return rule;
    }

    public static ValidationRule email() {
        return new ValidationRule(ValidationType.EMAIL.name());
    }

    public static ValidationRule positive() {
        return new ValidationRule(ValidationType.POSITIVE.name());
    }

    public static ValidationRule negative() {
        return new ValidationRule(ValidationType.NEGATIVE.name());
    }

    public static ValidationRule digits(int integer, int fraction) {
        ValidationRule rule = new ValidationRule(ValidationType.DIGITS.name());
        rule.parameters = Map.of("integer", integer, "fraction", fraction);
        return rule;
    }

    public static ValidationRule decimalMin(String value, boolean inclusive) {
        ValidationRule rule = new ValidationRule(ValidationType.DECIMAL_MIN.name());
        rule.parameters = Map.of("value", value, "inclusive", inclusive);
        return rule;
    }

    public static ValidationRule decimalMax(String value, boolean inclusive) {
        ValidationRule rule = new ValidationRule(ValidationType.DECIMAL_MAX.name());
        rule.parameters = Map.of("value", value, "inclusive", inclusive);
        return rule;
    }

    public static ValidationRule future() {
        return new ValidationRule(ValidationType.FUTURE.name());
    }

    public static ValidationRule past() {
        return new ValidationRule(ValidationType.PAST.name());
    }

    public static ValidationRule futureOrPresent() {
        return new ValidationRule(ValidationType.FUTURE_OR_PRESENT.name());
    }

    public static ValidationRule pastOrPresent() {
        return new ValidationRule(ValidationType.PAST_OR_PRESENT.name());
    }

    @Override
    public String toString() {
        return "ValidationRule{" +
                "type='" + type + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}