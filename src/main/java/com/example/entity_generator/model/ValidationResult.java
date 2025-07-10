package com.example.entity_generator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Result of metadata validation")
public class ValidationResult {
    @Schema(description = "Whether the validation was successful", example = "true")
    private boolean valid;

    @Schema(description = "List of validation errors")
    private List<String> errors = Collections.emptyList();

    @Schema(description = "List of validation warnings")
    private List<String> warnings = Collections.emptyList();

    public static ValidationResult valid() {
        return new ValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, List.of(message), Collections.emptyList());
    }
}