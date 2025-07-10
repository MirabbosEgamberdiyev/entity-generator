package com.example.entity_generator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Result of batch entity generation")
public class BatchGenerationResult {
    @Schema(description = "Total number of processed entities", example = "10")
    private int totalProcessed;

    @Schema(description = "Number of successfully generated entities", example = "8")
    private int successCount;

    @Schema(description = "Number of failed generations", example = "2")
    private int errorCount;

    @Schema(description = "List of generation results")
    private List<GenerationResult> results = Collections.emptyList();

    public static BatchGenerationResult error(String message) {
        BatchGenerationResult result = new BatchGenerationResult();
        result.errorCount = 1;
        result.results = List.of(GenerationResult.error(message));
        return result;
    }
}