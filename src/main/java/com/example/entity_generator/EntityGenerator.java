package com.example.entity_generator;

import com.example.entity_generator.model.*;
import com.example.entity_generator.service.EntityGeneratorService;
import com.example.entity_generator.service.ModelAnalyzerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generator")
@Tag(name = "Entity Generator", description = "API для генерации Entity классов и связанных компонентов")
public class EntityGenerator {

    @Autowired
    private EntityGeneratorService generatorService;

    @Autowired
    private ModelAnalyzerService analyzerService;

    @PostMapping("/generate")
    @Operation(summary = "Генерация Entity и связанных компонентов",
            description = "Генерирует Entity, DTO, Repository, Service, Controller на основе метаданных")
    @ApiResponse(responseCode = "200", description = "Успешная генерация")
    @ApiResponse(responseCode = "400", description = "Ошибка в данных")
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    public ResponseEntity<GenerationResult> generateEntity(
            @Valid @RequestBody EntityMetadata metadata,
            @Parameter(description = "Перезаписать существующие файлы")
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            GenerationResult result = generatorService.generateEntity(metadata, overwrite);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    GenerationResult.error("Ошибка генерации: " + e.getMessage())
                                                  );
        }
    }

    @PostMapping("/generate-from-model")
    @Operation(summary = "Генерация из Java модели",
            description = "Анализирует Java класс и генерирует компоненты")
    public ResponseEntity<GenerationResult> generateFromModel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            String content = new String(file.getBytes());
            EntityMetadata metadata = analyzerService.analyzeModel(content);
            GenerationResult result = generatorService.generateEntity(metadata, overwrite);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(400).body(
                    GenerationResult.error("Ошибка чтения файла: " + e.getMessage())
                                                  );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    GenerationResult.error("Ошибка генерации: " + e.getMessage())
                                                  );
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Валидация метаданных",
            description = "Проверяет корректность метаданных без генерации")
    public ResponseEntity<ValidationResult> validateMetadata(
            @Valid @RequestBody EntityMetadata metadata) {

        try {
            ValidationResult result = generatorService.validateMetadata(metadata);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ValidationResult.error("Ошибка валидации: " + e.getMessage())
                                                  );
        }
    }

    @GetMapping("/types")
    @Operation(summary = "Получить поддерживаемые типы данных")
    public ResponseEntity<List<String>> getSupportedTypes() {
        return ResponseEntity.ok(generatorService.getSupportedTypes());
    }

    @GetMapping("/validation-rules")
    @Operation(summary = "Получить поддерживаемые правила валидации")
    public ResponseEntity<List<String>> getSupportedValidationRules() {
        return ResponseEntity.ok(generatorService.getSupportedValidationRules());
    }

    @GetMapping("/relationship-types")
    @Operation(summary = "Получить типы связей")
    public ResponseEntity<List<String>> getRelationshipTypes() {
        return ResponseEntity.ok(generatorService.getRelationshipTypes());
    }

    @PostMapping("/generate-batch")
    @Operation(summary = "Пакетная генерация",
            description = "Генерирует несколько Entity одновременно")
    public ResponseEntity<BatchGenerationResult> generateBatch(
            @Valid @RequestBody List<EntityMetadata> metadataList,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            BatchGenerationResult result = generatorService.generateBatch(metadataList, overwrite);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    BatchGenerationResult.error("Ошибка пакетной генерации: " + e.getMessage())
                                                  );
        }
    }

    @GetMapping("/preview")
    @Operation(summary = "Предварительный просмотр",
            description = "Показывает код без сохранения файлов")
    public ResponseEntity<Map<String, String>> previewGeneration(
            @Valid @RequestBody EntityMetadata metadata) {

        try {
            Map<String, String> preview = generatorService.generatePreview(metadata);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Ошибка предварительного просмотра: " + e.getMessage())
                                                  );
        }
    }

    @DeleteMapping("/generated/{entityName}")
    @Operation(summary = "Удаление сгенерированных файлов")
    public ResponseEntity<String> deleteGenerated(
            @PathVariable String entityName) {

        try {
            generatorService.deleteGenerated(entityName);
            return ResponseEntity.ok("Файлы для " + entityName + " удалены");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    "Ошибка удаления: " + e.getMessage()
                                                  );
        }
    }

    @GetMapping("/generated")
    @Operation(summary = "Список сгенерированных Entity")
    public ResponseEntity<List<String>> getGeneratedEntities() throws IOException {
        return ResponseEntity.ok(generatorService.getGeneratedEntities());
    }

}