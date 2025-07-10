package com.example.entity_generator.service;

import com.example.entity_generator.exception.EntityGenerationException;
import com.example.entity_generator.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating entity-related components.
 * Current date and time: 03:19 PM +05, Thursday, July 10, 2025.
 */
@Service
public class EntityGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(EntityGeneratorService.class);
    private static final String DEFAULT_PACKAGE = "com.example.generated";
    private static final String SOURCE_ROOT = "src/main/java";

    /**
     * Generates entity and related components (DTO, Repository, Service, Controller).
     *
     * @param metadata The metadata for entity generation.
     * @param overwrite Whether to overwrite existing files.
     * @return Generation result with success status and generated file paths.
     * @throws EntityGenerationException If generation fails.
     */
    public GenerationResult generateEntity(EntityMetadata metadata, boolean overwrite) throws EntityGenerationException {
        logger.info("Starting entity generation for: {} at {}", metadata.getEntityName(), LocalDateTime.now());
        ValidationResult validation = validateMetadata(metadata);
        if (!validation.isValid()) {
            throw new EntityGenerationException("Validation failed: " + String.join(", ", validation.getErrors()));
        }

        List<String> generatedFiles = new ArrayList<>();
        try {
            String basePackage = DEFAULT_PACKAGE; // Har doim com.example.generated ishlatiladi
            String entityName = toSingular(metadata.getEntityName());

            generatedFiles.add(generateAndWriteFile(basePackage, "entity", entityName + ".java", generateEntityClassContent(metadata), overwrite));
            generatedFiles.add(generateAndWriteFile(basePackage, "dto", entityName + "DTO.java", generateDTOClassContent(metadata), overwrite));
            generatedFiles.add(generateAndWriteFile(basePackage, "repository", entityName + "Repository.java", generateRepositoryClassContent(metadata), overwrite));
            generatedFiles.add(generateAndWriteFile(basePackage, "service", entityName + "Service.java", generateServiceClassContent(metadata), overwrite));
            generatedFiles.add(generateAndWriteFile(basePackage, "controller", entityName + "Controller.java", generateControllerClassContent(metadata), overwrite));

            logger.info("Entity generation completed: {} at {}", generatedFiles, LocalDateTime.now());
            return GenerationResult.success("Entity successfully generated", generatedFiles);
        } catch (IOException e) {
            logger.error("Generation failed for entity: {}. Error: {}", metadata.getEntityName(), e.getMessage());
            throw new EntityGenerationException("Generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generates multiple entities in batch.
     *
     * @param metadataList List of entity metadata.
     * @param overwrite Whether to overwrite existing files.
     * @return Batch generation result.
     * @throws EntityGenerationException If batch generation fails.
     */
    public BatchGenerationResult generateBatch(List<EntityMetadata> metadataList, boolean overwrite) throws EntityGenerationException {
        logger.info("Starting batch generation for {} entities at {}", metadataList.size(), LocalDateTime.now());
        BatchGenerationResult result = new BatchGenerationResult();
        List<GenerationResult> results = new ArrayList<>();
        int successCount = 0, errorCount = 0;

        for (EntityMetadata metadata : metadataList) {
            GenerationResult singleResult = generateEntity(metadata, overwrite);
            results.add(singleResult);
            if (singleResult.isSuccess()) successCount++;
            else errorCount++;
        }

        result.setTotalProcessed(metadataList.size());
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setResults(results);
        logger.info("Batch generation completed: success={}, errors={} at {}", successCount, errorCount, LocalDateTime.now());
        return result;
    }

    /**
     * Validates the entity metadata.
     *
     * @param metadata The metadata to validate.
     * @return Validation result.
     */
    public ValidationResult validateMetadata(EntityMetadata metadata) {
        logger.info("Validating metadata for entity: {} at {}", metadata.getEntityName(), LocalDateTime.now());
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (isBlank(metadata.getEntityName())) {
            errors.add("Entity name is required");
        }
        if (metadata.getFields() == null || metadata.getFields().isEmpty()) {
            warnings.add("No fields defined; a default ID field will be added");
        } else {
            boolean hasId = metadata.getFields().stream().anyMatch(field -> Boolean.TRUE.equals(field.isPrimaryKey()));
            if (!hasId) {
                warnings.add("No primary key field found. An ID field will be automatically generated.");
            }

            for (Field field : metadata.getFields()) {
                if (isBlank(field.getName())) errors.add("Field name is required");
                if (isBlank(field.getType())) errors.add("Field type is required for field: " + field.getName());
            }
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    /**
     * Generates a preview of all generated components without saving files.
     *
     * @param metadata The metadata for entity generation.
     * @return Map of file names and their content.
     * @throws EntityGenerationException If preview generation fails.
     */
    public Map<String, String> generatePreview(EntityMetadata metadata) throws EntityGenerationException {
        logger.info("Generating preview for entity: {} at {}", metadata.getEntityName(), LocalDateTime.now());
        validateMetadata(metadata).isValid();

        Map<String, String> preview = new HashMap<>();
        try {
            String entityName = toSingular(metadata.getEntityName());
            preview.put("entity/" + entityName + ".java", generateEntityClassContent(metadata));
            preview.put("dto/" + entityName + "DTO.java", generateDTOClassContent(metadata));
            preview.put("repository/" + entityName + "Repository.java", generateRepositoryClassContent(metadata));
            preview.put("service/" + entityName + "Service.java", generateServiceClassContent(metadata));
            preview.put("controller/" + entityName + "Controller.java", generateControllerClassContent(metadata));
            return preview;
        } catch (Exception e) {
            logger.error("Preview generation failed: {} at {}", e.getMessage(), LocalDateTime.now());
            throw new EntityGenerationException("Preview generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a list of supported data types.
     *
     * @return List of supported types.
     */
    public List<String> getSupportedTypes() {
        logger.info("Fetching supported data types at {}", LocalDateTime.now());
        return Arrays.asList("String", "Integer", "Long", "Double", "Boolean", "LocalDate", "LocalDateTime");
    }

    /**
     * Returns a list of supported validation rules.
     *
     * @return List of validation rule names.
     */
    public List<String> getSupportedValidationRules() {
        logger.info("Fetching supported validation rules at {}", LocalDateTime.now());
        return Arrays.stream(ValidationRule.ValidationType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of supported relationship types.
     *
     * @return List of relationship types.
     */
    public List<String> getRelationshipTypes() {
        logger.info("Fetching supported relationship types at {}", LocalDateTime.now());
        return Arrays.asList("OneToOne", "OneToMany", "ManyToOne", "ManyToMany");
    }

    /**
     * Deletes generated files for a given entity.
     *
     * @param entityName Name of the entity to delete.
     * @throws IOException If file deletion fails.
     */
    public void deleteGenerated(String entityName) throws IOException {
        logger.info("Deleting generated files for entity: {} at {}", entityName, LocalDateTime.now());
        String basePath = SOURCE_ROOT + "/" + DEFAULT_PACKAGE.replace(".", "/");
        String singularName = toSingular(entityName);

        List<String> files = Arrays.asList(
                "entity/" + singularName + ".java",
                "dto/" + singularName + "DTO.java",
                "repository/" + singularName + "Repository.java",
                "service/" + singularName + "Service.java",
                "controller/" + singularName + "Controller.java"
                                          );

        for (String file : files) {
            Path path = Path.of(basePath + "/" + file);
            if (Files.exists(path)) {
                Files.delete(path);
                logger.debug("Deleted file: {} at {}", path, LocalDateTime.now());
            }
        }
    }

    /**
     * Retrieves a list of generated entities.
     *
     * @return List of entity names.
     * @throws IOException If directory reading fails.
     */
    public List<String> getGeneratedEntities() throws IOException {
        logger.info("Fetching generated entities at {}", LocalDateTime.now());
        Path entityPath = Path.of(SOURCE_ROOT + "/" + DEFAULT_PACKAGE.replace(".", "/") + "/entity");
        if (!Files.exists(entityPath)) {
            return Collections.emptyList();
        }
        return Files.list(entityPath)
                .filter(p -> p.toString().endsWith(".java"))
                .map(p -> p.getFileName().toString().replace(".java", ""))
                .collect(Collectors.toList());
    }

    /**
     * Generates and writes a file to the specified path within the project source.
     *
     * @param basePackage Base package name.
     * @param subPackage Sub-package (e.g., entity, dto).
     * @param fileName Name of the file.
     * @param content Content to write.
     * @param overwrite Whether to overwrite existing files.
     * @return Path of the written file.
     * @throws IOException If file writing fails.
     */
    private String generateAndWriteFile(String basePackage, String subPackage, String fileName, String content, boolean overwrite) throws IOException {
        Path basePath = Path.of(SOURCE_ROOT, basePackage.replace(".", "/"), subPackage);
        Files.createDirectories(basePath);
        Path path = basePath.resolve(fileName);

        if (Files.exists(path) && !overwrite) {
            logger.warn("File {} already exists and overwrite is disabled", path);
            return path.toString();
        }

        logger.info("Writing file: {} with content length: {}", path, content.length());
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        logger.info("Successfully wrote file: {}", path);
        return path.toString();
    }

    /**
     * Converts plural entity name to singular.
     *
     * @param entityName Entity name (possibly plural).
     * @return Singular form of the entity name.
     */
    private String toSingular(String entityName) {
        if (entityName == null || entityName.trim().isEmpty()) {
            return entityName;
        }
        String name = entityName.trim();
        if (name.equalsIgnoreCase("Xodimlar")) {
            return name;
        }
        if (name.toLowerCase().endsWith("s") && name.length() > 1) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * Generates the entity class content with Swagger annotations.
     *
     * @param metadata Entity metadata.
     * @return Generated Java code as a string.
     */
    private String generateEntityClassContent(EntityMetadata metadata) {
        String entityName = toSingular(metadata.getEntityName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(entityName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Entity.class).build())
                .addAnnotation(AnnotationSpec.builder(Table.class)
                        .addMember("name", "$S", "\"" + entityName.toLowerCase() + "\"")
                        .build())
                .addAnnotation(lombok.Data.class)
                .addAnnotation(lombok.NoArgsConstructor.class)
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.media.Schema.class)
                        .addMember("description", "$S", "Generated entity class for " + entityName)
                        .build());

        boolean hasId = metadata.getFields() != null &&
                metadata.getFields().stream().anyMatch(field -> Boolean.TRUE.equals(field.isPrimaryKey()));
        if (!hasId) {
            classBuilder.addField(FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
                    .addAnnotation(Id.class)
                    .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                            .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Column.class)
                            .addMember("name", "$S", "id")
                            .addMember("nullable", "$L", false)
                            .build())
                    .addAnnotation(AnnotationSpec.builder(Schema.class)
                            .addMember("description", "$S", "Unique identifier")
                            .build())
                    .build());
        }

        addAuditFields(classBuilder);

        if (metadata.getFields() != null && !metadata.getFields().isEmpty()) {
            for (Field field : metadata.getFields()) {
                validateField(field);
                classBuilder.addField(buildFieldSpec(field, metadata.isEnableValidation()));
            }
        }

        if (metadata.getRelationships() != null) {
            for (Relationship rel : metadata.getRelationships()) {
                validateRelationship(rel, entityName);
                classBuilder.addField(buildRelationshipFieldSpec(rel));
            }
        }

        String packageName = DEFAULT_PACKAGE + ".entity";
        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build()
                .toString();
    }

    /**
     * Generates the DTO class content with Swagger annotations.
     *
     * @param metadata Entity metadata.
     * @return Generated Java code as a string.
     */
    private String generateDTOClassContent(EntityMetadata metadata) {
        String entityName = toSingular(metadata.getEntityName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(entityName + "DTO")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(lombok.Data.class)
                .addAnnotation(AnnotationSpec.builder(Schema.class)
                        .addMember("description", "$S", "Data transfer object for " + entityName)
                        .build());

        classBuilder.addField(FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(Schema.class)
                        .addMember("description", "$S", "Unique identifier")
                        .addMember("example", "$S", "1")
                        .build())
                .build());

        classBuilder.addField(FieldSpec.builder(LocalDateTime.class, "createdAt", Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(Schema.class)
                        .addMember("description", "$S", "Creation timestamp")
                        .addMember("example", "$S", "2025-07-10T03:19:00")
                        .build())
                .build());

        classBuilder.addField(FieldSpec.builder(LocalDateTime.class, "updatedAt", Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(Schema.class)
                        .addMember("description", "$S", "Last update timestamp")
                        .addMember("example", "$S", "2025-07-10T03:19:00")
                        .build())
                .build());

        if (metadata.getFields() != null) {
            for (Field field : metadata.getFields()) {
                if (!Boolean.TRUE.equals(field.isPrimaryKey())) {
                    validateField(field);
                    classBuilder.addField(buildDTOFieldSpec(field, true, metadata.isEnableJsonAnnotations()));
                }
            }
        }

        String packageName = DEFAULT_PACKAGE + ".dto";
        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build()
                .toString();
    }

    /**
     * Generates the repository interface content.
     *
     * @param metadata Entity metadata.
     * @return Generated Java code as a string.
     */
    private String generateRepositoryClassContent(EntityMetadata metadata) {
        String entityName = toSingular(metadata.getEntityName());
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(entityName + "Repository")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(JpaRepository.class),
                        ClassName.get(DEFAULT_PACKAGE + ".entity", entityName),
                        ClassName.get(Long.class)))
                .addAnnotation(org.springframework.stereotype.Repository.class);

        String packageName = DEFAULT_PACKAGE + ".repository";
        return JavaFile.builder(packageName, interfaceBuilder.build())
                .indent("    ")
                .build()
                .toString();
    }

    /**
     * Generates the service class content.
     *
     * @param metadata Entity metadata.
     * @return Generated Java code as a string.
     */
    private String generateServiceClassContent(EntityMetadata metadata) {
        String entityName = toSingular(metadata.getEntityName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(entityName + "Service")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addAnnotation(lombok.RequiredArgsConstructor.class);

        classBuilder.addField(FieldSpec.builder(
                        ClassName.get(DEFAULT_PACKAGE + ".repository", entityName + "Repository"),
                        "repository", Modifier.PRIVATE, Modifier.FINAL)
                .build());

        addCrudMethods(classBuilder, entityName, metadata);

        String packageName = DEFAULT_PACKAGE + ".service";
        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build()
                .toString();
    }

    /**
     * Generates the controller class content with Swagger annotations.
     *
     * @param metadata Entity metadata.
     * @return Generated Java code as a string.
     */
    private String generateControllerClassContent(EntityMetadata metadata) {
        String entityName = toSingular(metadata.getEntityName());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(entityName + "Controller")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(org.springframework.web.bind.annotation.RestController.class)
                .addAnnotation(AnnotationSpec.builder(org.springframework.web.bind.annotation.RequestMapping.class)
                        .addMember("value", "$S", "/api/" + entityName.toLowerCase())
                        .build())
                .addAnnotation(lombok.RequiredArgsConstructor.class)
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.tags.Tag.class)
                        .addMember("name", "$S", entityName + " Controller")
                        .addMember("description", "$S", "API endpoints for " + entityName)
                        .build());

        classBuilder.addField(FieldSpec.builder(
                        ClassName.get(DEFAULT_PACKAGE + ".service", entityName + "Service"),
                        "service", Modifier.PRIVATE, Modifier.FINAL)
                .build());

        addCrudEndpoints(classBuilder, entityName, metadata);

        String packageName = DEFAULT_PACKAGE + ".controller";
        return JavaFile.builder(packageName, classBuilder.build())
                .indent("    ")
                .build()
                .toString();
    }

    /**
     * Adds audit fields (createdAt, updatedAt) to the class builder with current timestamp.
     *
     * @param classBuilder The class builder to modify.
     */
    private void addAuditFields(TypeSpec.Builder classBuilder) {
        LocalDateTime now = LocalDateTime.now();
        classBuilder.addField(FieldSpec.builder(LocalDateTime.class, "createdAt", Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(Column.class)
                        .addMember("name", "$S", "created_at")
                        .addMember("nullable", "$L", false)
                        .addMember("updatable", "$L", false)
                        .build())
                .build());

        classBuilder.addField(FieldSpec.builder(LocalDateTime.class, "updatedAt", Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(Column.class)
                        .addMember("name", "$S", "updated_at")
                        .addMember("nullable", "$L", false)
                        .build())
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("prePersist")
                .addAnnotation(PrePersist.class)
                .addModifiers(Modifier.PROTECTED)
                .addStatement("this.createdAt = $T.now()", LocalDateTime.class)
                .addStatement("this.updatedAt = $T.now()", LocalDateTime.class)
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("preUpdate")
                .addAnnotation(PreUpdate.class)
                .addModifiers(Modifier.PROTECTED)
                .addStatement("this.updatedAt = $T.now()", LocalDateTime.class)
                .build());
    }

    /**
     * Adds CRUD methods to the service class builder.
     *
     * @param classBuilder The class builder to modify.
     * @param entityName Name of the entity.
     * @param metadata Entity metadata.
     */
    private void addCrudMethods(TypeSpec.Builder classBuilder, String entityName, EntityMetadata metadata) {
        ClassName entityClass = ClassName.get(DEFAULT_PACKAGE + ".entity", entityName);

        classBuilder.addMethod(MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addStatement("return repository.findAll()")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC)
                .returns(entityClass)
                .addParameter(Long.class, "id")
                .addStatement("return repository.findById(id).orElseThrow(() -> new $T($S))",
                        EntityGenerationException.class, "Entity not found: " + entityName)
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .returns(entityClass)
                .addParameter(entityClass, "entity")
                .addStatement("return repository.save(entity)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("deleteById")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long.class, "id")
                .addStatement("repository.deleteById(id)")
                .build());
    }

    /**
     * Adds CRUD endpoints to the controller class builder.
     *
     * @param classBuilder The class builder to modify.
     * @param entityName Name of the entity.
     * @param metadata Entity metadata.
     */
    private void addCrudEndpoints(TypeSpec.Builder classBuilder, String entityName, EntityMetadata metadata) {
        ClassName entityClass = ClassName.get(DEFAULT_PACKAGE + ".entity", entityName);
        ClassName dtoClass = ClassName.get(DEFAULT_PACKAGE + ".dto", entityName + "DTO");

        classBuilder.addMethod(MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(org.springframework.web.bind.annotation.GetMapping.class)
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.Operation.class)
                        .addMember("summary", "$S", "Get all " + entityName)
                        .build())
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), dtoClass))
                .addStatement("$T result = service.findAll()", ParameterizedTypeName.get(ClassName.get(List.class), entityClass))
                .addStatement("return result.stream().map(this::toDTO).collect($T.toList())", Collectors.class)
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("getById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(org.springframework.web.bind.annotation.GetMapping.class)
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.Operation.class)
                        .addMember("summary", "$S", "Get " + entityName + " by ID")
                        .build())
                .addParameter(ParameterSpec.builder(Long.class, "id")
                        .addAnnotation(org.springframework.web.bind.annotation.PathVariable.class)
                        .build())
                .returns(dtoClass)
                .addStatement("$T entity = service.findById(id)", entityClass)
                .addStatement("return toDTO(entity)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(org.springframework.web.bind.annotation.PostMapping.class)
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.Operation.class)
                        .addMember("summary", "$S", "Create new " + entityName)
                        .build())
                .addParameter(ParameterSpec.builder(dtoClass, "dto")
                        .addAnnotation(org.springframework.web.bind.annotation.RequestBody.class)
                        .build())
                .returns(dtoClass)
                .addStatement("$T entity = toEntity(dto)", entityClass)
                .addStatement("$T savedEntity = service.save(entity)", entityClass)
                .addStatement("return toDTO(savedEntity)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(org.springframework.web.bind.annotation.PutMapping.class)
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.Operation.class)
                        .addMember("summary", "$S", "Update " + entityName)
                        .build())
                .addParameter(ParameterSpec.builder(Long.class, "id")
                        .addAnnotation(org.springframework.web.bind.annotation.PathVariable.class)
                        .build())
                .addParameter(ParameterSpec.builder(dtoClass, "dto")
                        .addAnnotation(org.springframework.web.bind.annotation.RequestBody.class)
                        .build())
                .returns(dtoClass)
                .addStatement("$T entity = toEntity(dto)", entityClass)
                .addStatement("entity.setId(id)")
                .addStatement("$T updatedEntity = service.save(entity)", entityClass)
                .addStatement("return toDTO(updatedEntity)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(org.springframework.web.bind.annotation.DeleteMapping.class)
                        .addMember("value", "$S", "/{id}")
                        .build())
                .addAnnotation(AnnotationSpec.builder(io.swagger.v3.oas.annotations.Operation.class)
                        .addMember("summary", "$S", "Delete " + entityName)
                        .build())
                .addParameter(ParameterSpec.builder(Long.class, "id")
                        .addAnnotation(org.springframework.web.bind.annotation.PathVariable.class)
                        .build())
                .addStatement("service.deleteById(id)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("toDTO")
                .addModifiers(Modifier.PRIVATE)
                .returns(dtoClass)
                .addParameter(entityClass, "entity")
                .addStatement("$T dto = new $T()", dtoClass, dtoClass)
                .addStatement("dto.setId(entity.getId())")
                .beginControlFlow("if (entity.getCreatedAt() != null)")
                .addStatement("dto.setCreatedAt(entity.getCreatedAt())")
                .endControlFlow()
                .beginControlFlow("if (entity.getUpdatedAt() != null)")
                .addStatement("dto.setUpdatedAt(entity.getUpdatedAt())")
                .endControlFlow()
                .addCode(generateFieldMappingCode(metadata, true))
                .addStatement("return dto")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("toEntity")
                .addModifiers(Modifier.PRIVATE)
                .returns(entityClass)
                .addParameter(dtoClass, "dto")
                .addStatement("$T entity = new $T()", entityClass, entityClass)
                .addStatement("entity.setId(dto.getId())")
                .beginControlFlow("if (dto.getCreatedAt() != null)")
                .addStatement("entity.setCreatedAt(dto.getCreatedAt())")
                .endControlFlow()
                .beginControlFlow("if (dto.getUpdatedAt() != null)")
                .addStatement("entity.setUpdatedAt(dto.getUpdatedAt())")
                .endControlFlow()
                .addCode(generateFieldMappingCode(metadata, false))
                .addStatement("return entity")
                .build());
    }

    private CodeBlock generateFieldMappingCode(EntityMetadata metadata, boolean toDTO) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        if (metadata.getFields() != null) {
            for (Field field : metadata.getFields()) {
                if (!Boolean.TRUE.equals(field.isPrimaryKey())) {
                    String fieldName = field.getName().trim();
                    String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    if (toDTO) {
                        codeBuilder.addStatement("dto.set" + capitalizedFieldName + "(entity.get" + capitalizedFieldName + "())");
                    } else {
                        codeBuilder.addStatement("entity.set" + capitalizedFieldName + "(dto.get" + capitalizedFieldName + "())");
                    }
                }
            }
        }
        return codeBuilder.build();
    }

    /**
     * Validates a field metadata.
     *
     * @param field Field metadata to validate.
     * @throws EntityGenerationException If validation fails.
     */
    private void validateField(Field field) {
        if (isBlank(field.getName())) throw new EntityGenerationException("Field name is required");
        if (isBlank(field.getType())) throw new EntityGenerationException("Field type is required for field: " + field.getName());
    }

    /**
     * Validates a relationship metadata.
     *
     * @param rel Relationship metadata to validate.
     * @param entityName Name of the entity.
     * @throws EntityGenerationException If validation fails.
     */
    private void validateRelationship(Relationship rel, String entityName) {
        if (isBlank(rel.getSourceField())) throw new EntityGenerationException("Source field is required for relationship in entity: " + entityName);
        if (isBlank(rel.getTargetEntity())) throw new EntityGenerationException("Target entity is required for relationship in entity: " + entityName);
    }

    /**
     * Builds a field specification for the entity class.
     *
     * @param field Field metadata.
     * @param enableValidation Whether validation annotations are enabled.
     * @return FieldSpec for the entity.
     */
    private FieldSpec buildFieldSpec(Field field, boolean enableValidation) {
        TypeName fieldType = ClassName.bestGuess(field.getType().trim());
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName().trim(), Modifier.PRIVATE);

        if (Boolean.TRUE.equals(field.isPrimaryKey())) {
            fieldBuilder.addAnnotation(Id.class)
                    .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                            .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                            .build());
        }

        AnnotationSpec.Builder columnBuilder = AnnotationSpec.builder(Column.class);
        columnBuilder.addMember("name", "$S", field.getName().trim());
        if (Boolean.FALSE.equals(field.isNullable())) columnBuilder.addMember("nullable", "$L", false);
        if (Boolean.TRUE.equals(field.isUnique())) columnBuilder.addMember("unique", "$L", true);
        if (field.getLength() != null && field.getLength() > 0) columnBuilder.addMember("length", "$L", field.getLength());
        fieldBuilder.addAnnotation(columnBuilder.build());

        if (enableValidation && field.getValidations() != null) {
            for (ValidationRule rule : field.getValidations()) {
                AnnotationSpec annotation = buildValidationAnnotation(rule);
                if (annotation != null) fieldBuilder.addAnnotation(annotation);
            }
        }

        fieldBuilder.addAnnotation(AnnotationSpec.builder(Schema.class)
                .addMember("description", "$S", "Field " + field.getName())
                .build());

        return fieldBuilder.build();
    }

    /**
     * Builds a field specification for the DTO class.
     *
     * @param field Field metadata.
     * @param enableSwagger Whether Swagger annotations are enabled.
     * @param enableJsonAnnotations Whether JSON annotations are enabled.
     * @return FieldSpec for the DTO.
     */
    private FieldSpec buildDTOFieldSpec(Field field, boolean enableSwagger, boolean enableJsonAnnotations) {
        TypeName fieldType = ClassName.bestGuess(field.getType().trim());
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName().trim(), Modifier.PRIVATE);

        if (enableSwagger && field.getSwaggerConfig() != null) {
            Field.SwaggerConfig swagger = field.getSwaggerConfig();
            AnnotationSpec.Builder schemaBuilder = AnnotationSpec.builder(Schema.class);
            if (swagger.getDescription() != null) schemaBuilder.addMember("description", "$S", swagger.getDescription());
            if (swagger.isRequired()) schemaBuilder.addMember("required", "$L", true);
            if (swagger.getExample() != null) schemaBuilder.addMember("example", "$S", swagger.getExample());
            if (swagger.getFormat() != null) schemaBuilder.addMember("format", "$S", swagger.getFormat());
            fieldBuilder.addAnnotation(schemaBuilder.build());
        } else {
            fieldBuilder.addAnnotation(AnnotationSpec.builder(Schema.class)
                    .addMember("description", "$S", "Field " + field.getName())
                    .build());
        }

        if (enableJsonAnnotations && field.getJsonConfig() != null) {
            Field.JsonConfig json = field.getJsonConfig();
            if (json.getPropertyName() != null) {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", json.getPropertyName())
                        .build());
            }
            if (json.isIgnore()) fieldBuilder.addAnnotation(JsonIgnore.class);
            if (json.getFormat() != null) {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonFormat.class)
                        .addMember("shape", "$T.OBJECT", JsonFormat.Shape.class)
                        .addMember("pattern", "$S", json.getFormat())
                        .build());
            }
        }

        return fieldBuilder.build();
    }

    /**
     * Builds a relationship field specification.
     *
     * @param rel Relationship metadata.
     * @return FieldSpec for the relationship.
     */
    private FieldSpec buildRelationshipFieldSpec(Relationship rel) {
        TypeName targetType = ClassName.bestGuess(rel.getTargetEntity().trim());
        AnnotationSpec.Builder relAnnotation = AnnotationSpec.builder(getRelationshipAnnotation(rel.getType()));
        if (rel.getMappedBy() != null && !rel.getMappedBy().trim().isEmpty()) {
            relAnnotation.addMember("mappedBy", "$S", rel.getMappedBy().trim());
        }
        if (rel.getFetch() != null && !rel.getFetch().trim().isEmpty()) {
            relAnnotation.addMember("fetch", "$T.$L", FetchType.class, rel.getFetch().trim().toUpperCase());
        }
        if (rel.getCascade() != null && !rel.getCascade().isEmpty()) {
            CodeBlock cascadeBlock = CodeBlock.builder()
                    .add("{")
                    .add(String.join(", ", rel.getCascade().stream()
                            .map(c -> "$T." + c.trim().toUpperCase())
                            .collect(Collectors.toList())), CascadeType.class)
                    .add("}")
                    .build();
            relAnnotation.addMember("cascade", cascadeBlock);
        }
        if (!rel.isOptional()) {
            relAnnotation.addMember("optional", "$L", false);
        }

        return FieldSpec.builder(targetType, rel.getSourceField().trim(), Modifier.PRIVATE)
                .addAnnotation(relAnnotation.build())
                .addAnnotation(AnnotationSpec.builder(Schema.class)
                        .addMember("description", "$S", "Relationship field " + rel.getSourceField())
                        .build())
                .build();
    }

    /**
     * Builds a validation annotation based on the rule.
     *
     * @param rule Validation rule.
     * @return AnnotationSpec or null if not supported.
     */
    private AnnotationSpec buildValidationAnnotation(ValidationRule rule) {
        AnnotationSpec.Builder builder = null;
        switch (rule.getType()) {
            case "NotNull":
                builder = AnnotationSpec.builder(NotNull.class);
                break;
            case "NotBlank":
                builder = AnnotationSpec.builder(NotBlank.class);
                break;
            case "Size":
                builder = AnnotationSpec.builder(Size.class);
                if (rule.getParameters() != null) {
                    AnnotationSpec.Builder finalBuilder = builder;
                    rule.getParameters().forEach((key, value) -> {
                        if ("min".equals(key)) finalBuilder.addMember("min", "$L", value);
                        if ("max".equals(key)) finalBuilder.addMember("max", "$L", value);
                    });
                }
                break;
            case "Pattern":
                builder = AnnotationSpec.builder(Pattern.class);
                if (rule.getParameters() != null && rule.getParameters().containsKey("regexp")) {
                    builder.addMember("regexp", "$S", rule.getParameters().get("regexp").toString());
                }
                break;
            case "Min":
                builder = AnnotationSpec.builder(Min.class);
                if (rule.getParameters() != null && rule.getParameters().containsKey("value")) {
                    builder.addMember("value", "$L", rule.getParameters().get("value"));
                }
                break;
            case "Max":
                builder = AnnotationSpec.builder(Max.class);
                if (rule.getParameters() != null && rule.getParameters().containsKey("value")) {
                    builder.addMember("value", "$L", rule.getParameters().get("value"));
                }
                break;
            case "Email":
                builder = AnnotationSpec.builder(Email.class);
                break;
            case "Positive":
                builder = AnnotationSpec.builder(Positive.class);
                break;
            case "Negative":
                builder = AnnotationSpec.builder(Negative.class);
                break;
            case "Digits":
                builder = AnnotationSpec.builder(Digits.class);
                if (rule.getParameters() != null) {
                    if (rule.getParameters().containsKey("integer")) {
                        builder.addMember("integer", "$L", rule.getParameters().get("integer"));
                    }
                    if (rule.getParameters().containsKey("fraction")) {
                        builder.addMember("fraction", "$L", rule.getParameters().get("fraction"));
                    }
                }
                break;
        }
        if (builder != null && rule.getMessage() != null) {
            builder.addMember("message", "$S", rule.getMessage());
        }
        return builder != null ? builder.build() : null;
    }

    /**
     * Returns the annotation class for a given relationship type.
     *
     * @param type Relationship type.
     * @return Annotation class.
     * @throws EntityGenerationException If type is unsupported.
     */
    private Class<?> getRelationshipAnnotation(String type) {
        return switch (type) {
            case "OneToOne" -> OneToOne.class;
            case "OneToMany" -> OneToMany.class;
            case "ManyToOne" -> ManyToOne.class;
            case "ManyToMany" -> ManyToMany.class;
            default -> throw new EntityGenerationException("Unsupported relationship type: " + type);
        };
    }

    /**
     * Checks if a string is null or blank.
     *
     * @param str String to check.
     * @return True if string is null or blank, false otherwise.
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}