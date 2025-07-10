package com.example.entity_generator;

import com.example.entity_generator.exception.EntityGenerationException;
import com.example.entity_generator.model.*;
import com.example.entity_generator.service.EntityGeneratorService;
import com.example.entity_generator.service.ModelAnalyzerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generator")
@Tag(name = "Entity Generator", description = "API for generating entity classes and components")
public class EntityGenerator {

    private static final Logger logger = LoggerFactory.getLogger(EntityGenerator.class);

    private final EntityGeneratorService generatorService;
    private final ModelAnalyzerService analyzerService;

    public EntityGenerator(EntityGeneratorService generatorService, ModelAnalyzerService analyzerService) {
        this.generatorService = generatorService;
        this.analyzerService = analyzerService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate entity and components", description = "Generates Entity, DTO, Repository, Service, and Controller")
    @ApiResponse(responseCode = "200", description = "Successful generation")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<GenerationResult> generateEntity(
            @Valid @RequestBody EntityMetadata metadata,
            @Parameter(description = "Overwrite existing files") @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            logger.info("Generating entity: {} at {}", metadata.getEntityName(), java.time.LocalDateTime.now());
            return ResponseEntity.ok(generatorService.generateEntity(metadata, overwrite));
        } catch (EntityGenerationException e) {
            logger.error("Generation failed for entity: {}. Error: {}", metadata.getEntityName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenerationResult.error(e.getMessage()));
        }
    }

    @PostMapping("/generate-from-model")
    @Operation(summary = "Generate from Java model", description = "Analyzes Java class and generates components")
    public ResponseEntity<GenerationResult> generateFromModel(
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Overwrite existing files") @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            logger.info("Generating from model: {} at {}", file.getOriginalFilename(), java.time.LocalDateTime.now());

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenerationResult.error("File is empty"));
            }
            if (!"text/x-java-source".equals(file.getContentType()) && !"application/java".equals(file.getContentType()) && !"application/octet-stream".equals(file.getContentType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenerationResult.error("Only Java files are allowed"));
            }
            if (file.getSize() > 1_000_000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(GenerationResult.error("File size exceeds 1MB limit"));
            }

            String javaCode = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            EntityMetadata metadata = analyzerService.analyzeModel(javaCode);
            return ResponseEntity.ok(generatorService.generateEntity(metadata, overwrite));
        } catch (IOException e) {
            logger.error("File reading error: {} at {}", e.getMessage(), java.time.LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenerationResult.error("File reading error: " + e.getMessage()));
        } catch (EntityGenerationException e) {
            logger.error("Generation failed: {} at {}", e.getMessage(), java.time.LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenerationResult.error(e.getMessage()));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate metadata", description = "Validates metadata without generating files")
    public ResponseEntity<ValidationResult> validateMetadata(@Valid @RequestBody EntityMetadata metadata) {
        try {
            logger.info("Validating metadata for entity: {}", metadata.getEntityName());
            return ResponseEntity.ok(generatorService.validateMetadata(metadata));
        } catch (EntityGenerationException e) {
            logger.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ValidationResult.error(e.getMessage()));
        }
    }

    @GetMapping("/types")
    @Operation(summary = "Get supported data types")
    public ResponseEntity<List<String>> getSupportedTypes() {
        logger.info("Fetching supported data types");
        return ResponseEntity.ok(generatorService.getSupportedTypes());
    }

    @GetMapping("/validation-rules")
    @Operation(summary = "Get supported validation rules")
    public ResponseEntity<List<String>> getSupportedValidationRules() {
        logger.info("Fetching supported validation rules");
        return ResponseEntity.ok(generatorService.getSupportedValidationRules());
    }

    @GetMapping("/relationship-types")
    @Operation(summary = "Get supported relationship types")
    public ResponseEntity<List<String>> getRelationshipTypes() {
        logger.info("Fetching supported relationship types");
        return ResponseEntity.ok(generatorService.getRelationshipTypes());
    }

    @PostMapping("/generate-batch")
    @Operation(summary = "Batch generation", description = "Generates multiple entities")
    public ResponseEntity<BatchGenerationResult> generateBatch(
            @Valid @RequestBody List<EntityMetadata> metadataList,
            @Parameter(description = "Overwrite existing files") @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            logger.info("Starting batch generation for {} entities", metadataList.size());
            return ResponseEntity.ok(generatorService.generateBatch(metadataList, overwrite));
        } catch (EntityGenerationException e) {
            logger.error("Batch generation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BatchGenerationResult.error(e.getMessage()));
        }
    }

    @GetMapping("/preview")
    @Operation(summary = "Preview generation", description = "Shows generated code without saving files")
    public ResponseEntity<Map<String, String>> previewGeneration(@Valid @RequestBody EntityMetadata metadata) {
        try {
            logger.info("Generating preview for entity: {}", metadata.getEntityName());
            return ResponseEntity.ok(generatorService.generatePreview(metadata));
        } catch (EntityGenerationException e) {
            logger.error("Preview generation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/generated/{entityName}")
    @Operation(summary = "Delete generated files")
    public ResponseEntity<String> deleteGenerated(@PathVariable String entityName) {
        try {
            logger.info("Deleting generated files for entity: {}", entityName);
            generatorService.deleteGenerated(entityName);
            return ResponseEntity.ok("Files for " + entityName + " deleted successfully");
        } catch (IOException e) {
            logger.error("Deletion failed for entity: {}. Error: {}", entityName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Deletion error: " + e.getMessage());
        }
    }

    @GetMapping("/generated")
    @Operation(summary = "List generated entities")
    public ResponseEntity<List<String>> getGeneratedEntities() {
        try {
            logger.info("Fetching generated entities");
            return ResponseEntity.ok(generatorService.getGeneratedEntities());
        } catch (IOException e) {
            logger.error("Error fetching generated entities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Error fetching entities: " + e.getMessage()));
        }
    }
}