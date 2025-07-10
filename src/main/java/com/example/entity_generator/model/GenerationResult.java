package com.example.entity_generator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Result of entity generation")
public class GenerationResult {
    @Schema(description = "Whether the generation was successful", example = "true")
    private boolean success;

    @Schema(description = "Message describing the result", example = "Entity successfully generated")
    private String message;

    @Schema(description = "List of generated file paths")
    private List<String> generatedFiles = Collections.emptyList();

    @Schema(description = "Map of errors, if any")
    private Map<String, String> errors = Collections.emptyMap();

    public static GenerationResult success(String message, List<String> files) {
        return new GenerationResult(true, message, files, Collections.emptyMap());
    }

    public static GenerationResult error(String message) {
        return new GenerationResult(false, message, Collections.emptyList(), Map.of("error", message));
    }

    @Override
    public String toString() {
        return "GenerationResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", generatedFiles=" + generatedFiles +
                '}';
    }
}