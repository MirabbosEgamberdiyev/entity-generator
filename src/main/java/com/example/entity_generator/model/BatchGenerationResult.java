package com.example.entity_generator.model;

import java.util.List;

public  class BatchGenerationResult {
    private int totalProcessed;
    private int successCount;
    private int errorCount;
    private List<GenerationResult> results;

    public static BatchGenerationResult error(String message) {
        BatchGenerationResult result = new BatchGenerationResult();
        result.errorCount = 1;
        result.results = List.of(GenerationResult.error(message));
        return result;
    }

    // Getters and Setters
    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public List<GenerationResult> getResults() { return results; }
    public void setResults(List<GenerationResult> results) { this.results = results; }
}
