package com.example.entity_generator.service;


import com.example.entity_generator.model.*;
import com.squareup.javapoet.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EntityGeneratorService {

    private static final String GENERATED_PACKAGE = "com.example.generated";

    public GenerationResult generateEntity(EntityMetadata metadata, boolean overwrite) throws IOException {
        List<String> generatedFiles = new ArrayList<>();
        Map<String, String> errors = new HashMap<>();

        try {
            // Validate metadata
            ValidationResult validationResult = validateMetadata(metadata);
            if (!validationResult.isValid()) {
                return GenerationResult.error(validationResult.getErrors().toString());
            }

            // Generate Entity
            String entityFile = generateEntityClass(metadata);
            generatedFiles.add(entityFile);

            // Generate DTO
            String dtoFile = generateDTOClass(metadata);
            generatedFiles.add(dtoFile);

            // Generate Repository
            String repoFile = generateRepositoryClass(metadata);
            generatedFiles.add(repoFile);

            // Generate Service
            String serviceFile = generateServiceClass(metadata);
            generatedFiles.add(serviceFile);

            // Generate Controller
            String controllerFile = generateControllerClass(metadata);
            generatedFiles.add(controllerFile);

            return GenerationResult.success("Entity successfully generated", generatedFiles);
        } catch (Exception e) {
            errors.put(metadata.getEntityName(), e.getMessage());
            return GenerationResult.error("Generation failed: " + e.getMessage());
        }
    }

    public BatchGenerationResult generateBatch(List<EntityMetadata> metadataList, boolean overwrite) throws IOException {
        BatchGenerationResult result = new BatchGenerationResult();
        List<GenerationResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (EntityMetadata metadata : metadataList) {
            GenerationResult singleResult = generateEntity(metadata, overwrite);
            results.add(singleResult);
            if (singleResult.isSuccess()) {
                successCount++;
            } else {
                errorCount++;
            }
        }

        result.setTotalProcessed(metadataList.size());
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setResults(results);
        return result;
    }

    public ValidationResult validateMetadata(EntityMetadata metadata) {
        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (metadata.getEntityName() == null || metadata.getEntityName().isBlank()) {
            errors.add("Entity name is required");
        }
        if (metadata.getFields() == null || metadata.getFields().isEmpty()) {
            errors.add("At least one field is required");
        } else {
            for (Field field : metadata.getFields()) {
                if (field.getName() == null || field.getName().isBlank()) {
                    errors.add("Field name is required");
                }
                if (field.getType() == null || field.getType().isBlank()) {
                    errors.add("Field type is required for field: " + field.getName());
                }
            }
        }

        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        result.setWarnings(warnings);
        return result;
    }

    public Map<String, String> generatePreview(EntityMetadata metadata) throws IOException {
        Map<String, String> preview = new HashMap<>();
        preview.put(metadata.getEntityName() + ".java", generateEntityClassContent(metadata));
        preview.put(metadata.getEntityName() + "DTO.java", generateDTOClassContent(metadata));
        preview.put(metadata.getEntityName() + "Repository.java", generateRepositoryClassContent(metadata));
        preview.put(metadata.getEntityName() + "Service.java", generateServiceClassContent(metadata));
        preview.put(metadata.getEntityName() + "Controller.java", generateControllerClassContent(metadata));
        return preview;
    }

    public List<String> getSupportedTypes() {
        return Arrays.asList("String", "Integer", "Long", "Double", "Boolean", "LocalDate", "LocalDateTime");
    }

    public List<String> getSupportedValidationRules() {
        return Arrays.asList("NotNull", "NotBlank", "Size", "Pattern", "Min", "Max", "Email", "Positive", "Negative",
                "Digits", "DecimalMin", "DecimalMax", "Future", "Past", "FutureOrPresent", "PastOrPresent");
    }

    public List<String> getRelationshipTypes() {
        return Arrays.asList("OneToOne", "OneToMany", "ManyToOne", "ManyToMany");
    }

    public void deleteGenerated(String entityName) throws IOException {
        // Implement file deletion logic
        String packagePath = GENERATED_PACKAGE.replace(".", "/");
        List<String> files = Arrays.asList(
                entityName + ".java",
                entityName + "DTO.java",
                entityName + "Repository.java",
                entityName + "Service.java",
                entityName + "Controller.java"
                                          );
        for (String file : files) {
            Path path = Path.of("src/main/java/" + packagePath + "/" + file);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }

    public List<String> getGeneratedEntities() throws IOException {
        String packagePath = GENERATED_PACKAGE.replace(".", "/");
        Path path = Path.of("src/main/java/" + packagePath);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        return Files.list(path)
                .filter(p -> p.toString().endsWith(".java"))
                .map(p -> p.getFileName().toString().replace(".java", ""))
                .filter(name -> !name.contains("DTO") && !name.contains("Repository") &&
                        !name.contains("Service") && !name.contains("Controller"))
                .collect(Collectors.toList());
    }

    private String generateEntityClass(EntityMetadata metadata) throws IOException {
        String content = generateEntityClassContent(metadata);
        String packagePath = (metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE).replace(".", "/");
        Path path = Path.of("src/main/java/" + packagePath + "/" + metadata.getEntityName() + ".java");
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path.toString();
    }

    private String generateDTOClass(EntityMetadata metadata) throws IOException {
        String content = generateDTOClassContent(metadata);
        String packagePath = (metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE).replace(".", "/");
        Path path = Path.of("src/main/java/" + packagePath + "/" + metadata.getEntityName() + "DTO.java");
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path.toString();
    }

    private String generateRepositoryClass(EntityMetadata metadata) throws IOException {
        String content = generateRepositoryClassContent(metadata);
        String packagePath = (metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE).replace(".", "/");
        Path path = Path.of("src/main/java/" + packagePath + "/" + metadata.getEntityName() + "Repository.java");
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path.toString();
    }

    private String generateServiceClass(EntityMetadata metadata) throws IOException {
        String content = generateServiceClassContent(metadata);
        String packagePath = (metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE).replace(".", "/");
        Path path = Path.of("src/main/java/" + packagePath + "/" + metadata.getEntityName() + "Service.java");
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path.toString();
    }

    private String generateControllerClass(EntityMetadata metadata) throws IOException {
        String content = generateControllerClassContent(metadata);
        String packagePath = (metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE).replace(".", "/");
        Path path = Path.of("src/main/java/" + packagePath + "/" + metadata.getEntityName() + "Controller.java");
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path.toString();
    }

    private String generateEntityClassContent(EntityMetadata metadata) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(metadata.getEntityName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addAnnotation(AnnotationSpec.builder(Table.class)
                        .addMember("name", "$S", metadata.getEntityName().toLowerCase())
                        .build());

        // Fields
        for (Field field : metadata.getFields()) {
            TypeName fieldType = ClassName.bestGuess(field.getType());
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName(), Modifier.PRIVATE);

            // Primary Key
            if (field.isPrimaryKey()) {
                fieldBuilder.addAnnotation(Id.class);
                fieldBuilder.addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                        .addMember("strategy", "$T.AUTO", GenerationType.class)
                        .build());
            }

            // Column
            AnnotationSpec.Builder columnBuilder = AnnotationSpec.builder(Column.class);
            if (field.getColumnName() != null) {
                columnBuilder.addMember("name", "$S", field.getColumnName());
            }
            if (!field.isNullable()) {
                columnBuilder.addMember("nullable", "$L", false);
            }
            if (field.isUnique()) {
                columnBuilder.addMember("unique", "$L", true);
            }
            if (field.getLength() != null) {
                columnBuilder.addMember("length", "$L", field.getLength());
            }
            fieldBuilder.addAnnotation(columnBuilder.build());

            // Validations
            if (metadata.isEnableValidation() && field.getValidations() != null) {
                for (ValidationRule rule : field.getValidations()) {
                    AnnotationSpec annotation = buildValidationAnnotation(rule);
                    if (annotation != null) {
                        fieldBuilder.addAnnotation(annotation);
                    }
                }
            }

            classBuilder.addField(fieldBuilder.build());
        }

        // Relationships
        if (metadata.getRelationships() != null) {
            for (Relationship rel : metadata.getRelationships()) {
                TypeName targetType = ClassName.bestGuess(rel.getTargetEntity());
                AnnotationSpec.Builder relAnnotation = AnnotationSpec.builder(getRelationshipAnnotation(rel.getType()));
                if (rel.getMappedBy() != null) {
                    relAnnotation.addMember("mappedBy", "$S", rel.getMappedBy());
                }
                if (rel.getFetch() != null) {
                    relAnnotation.addMember("fetch", "$T.$L", FetchType.class, rel.getFetch());
                }
                if (rel.getCascade() != null && !rel.getCascade().isEmpty()) {
                    CodeBlock cascadeBlock = CodeBlock.builder()
                            .add("{")
                            .add(String.join(", ", rel.getCascade().stream()
                                    .map(c -> "$T." + c)
                                    .collect(Collectors.toList())), CascadeType.class)
                            .add("}")
                            .build();
                    relAnnotation.addMember("cascade", cascadeBlock);
                }
                if (!rel.isOptional()) {
                    relAnnotation.addMember("optional", "$L", false);
                }
                FieldSpec fieldSpec = FieldSpec.builder(targetType, rel.getSourceField(), Modifier.PRIVATE)
                        .addAnnotation(relAnnotation.build())
                        .build();
                classBuilder.addField(fieldSpec);
            }
        }

        // Getters and Setters
        for (Field field : metadata.getFields()) {
            classBuilder.addMethod(MethodSpec.methodBuilder("get" + capitalize(field.getName()))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess(field.getType()))
                    .addStatement("return $N", field.getName())
                    .build());
            classBuilder.addMethod(MethodSpec.methodBuilder("set" + capitalize(field.getName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.bestGuess(field.getType()), field.getName())
                    .addStatement("this.$N = $N", field.getName(), field.getName())
                    .build());
        }

        String packageName = metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE;
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        return javaFile.toString();
    }

    private String generateDTOClassContent(EntityMetadata metadata) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(metadata.getEntityName() + "DTO")
                .addModifiers(Modifier.PUBLIC);

        // Fields
        for (Field field : metadata.getFields()) {
            TypeName fieldType = ClassName.bestGuess(field.getType());
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName(), Modifier.PRIVATE);

            // Swagger Annotations
            if (metadata.isEnableSwagger() && field.getSwaggerConfig() != null) {
                Field.SwaggerConfig swagger = field.getSwaggerConfig();
                AnnotationSpec.Builder schemaBuilder = AnnotationSpec.builder(Schema.class);
                if (swagger.getDescription() != null) {
                    schemaBuilder.addMember("description", "$S", swagger.getDescription());
                }
                if (swagger.isRequired()) {
                    schemaBuilder.addMember("required", "$L", true);
                }
                if (swagger.getExample() != null) {
                    schemaBuilder.addMember("example", "$S", swagger.getExample());
                }
                if (swagger.getFormat() != null) {
                    schemaBuilder.addMember("format", "$S", swagger.getFormat());
                }
                fieldBuilder.addAnnotation(schemaBuilder.build());
            }

            // JSON Annotations
            if (metadata.isEnableJsonAnnotations() && field.getJsonConfig() != null) {
                Field.JsonConfig json = field.getJsonConfig();
                if (json.getPropertyName() != null) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                            .addMember("value", "$S", json.getPropertyName())
                            .build());
                }
                if (json.isIgnore()) {
                    fieldBuilder.addAnnotation(JsonIgnore.class);
                }
                if (json.getFormat() != null) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(JsonFormat.class)
                            .addMember("shape", "$T.OBJECT", JsonFormat.Shape.class)
                            .addMember("pattern", "$S", json.getFormat())
                            .build());
                }
            }

            classBuilder.addField(fieldBuilder.build());
        }

        // Getters and Setters
        for (Field field : metadata.getFields()) {
            classBuilder.addMethod(MethodSpec.methodBuilder("get" + capitalize(field.getName()))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess(field.getType()))
                    .addStatement("return $N", field.getName())
                    .build());
            classBuilder.addMethod(MethodSpec.methodBuilder("set" + capitalize(field.getName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.bestGuess(field.getType()), field.getName())
                    .addStatement("this.$N = $N", field.getName(), field.getName())
                    .build());
        }

        String packageName = metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE;
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        return javaFile.toString();
    }

    private String generateRepositoryClassContent(EntityMetadata metadata) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(metadata.getEntityName() + "Repository")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(JpaRepository.class),
                        ClassName.bestGuess(metadata.getEntityName()),
                        ClassName.get(Long.class)))
                .addAnnotation(Repository.class);

        String packageName = metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE;
        JavaFile javaFile = JavaFile.builder(packageName, interfaceBuilder.build()).build();
        return javaFile.toString();
    }

    private String generateServiceClassContent(EntityMetadata metadata) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(metadata.getEntityName() + "Service")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class);

        // Fields
        classBuilder.addField(FieldSpec.builder(
                        ClassName.bestGuess(metadata.getEntityName() + "Repository"),
                        "repository", Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(Autowired.class)
                .build());

        // CRUD Methods
        classBuilder.addMethod(MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.bestGuess(metadata.getEntityName())))
                .addStatement("return repository.findAll()")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(metadata.getEntityName()))
                .addParameter(Long.class, "id")
                .addStatement("return repository.findById(id).orElse(null)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(metadata.getEntityName()))
                .addParameter(ClassName.bestGuess(metadata.getEntityName()), "entity")
                .addStatement("return repository.save(entity)")
                .build());

        classBuilder.addMethod(MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long.class, "id")
                .addStatement("repository.deleteById(id)")
                .build());

        String packageName = metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE;
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        return javaFile.toString();
    }

    private String generateControllerClassContent(EntityMetadata metadata) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(metadata.getEntityName() + "Controller")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "$S", "/api/" + metadata.getEntityName().toLowerCase())
                        .build());

        // Fields
        classBuilder.addField(FieldSpec.builder(
                        ClassName.bestGuess(metadata.getEntityName() + "Service"),
                        "service", Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(Autowired.class)
                .build());

        // CRUD Endpoints
        classBuilder.addMethod(MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GetMapping.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), ClassName.bestGuess(metadata.getEntityName())))
                .addStatement("return service.findAll()")
                .build());


        String packageName = metadata.getPackageName() != null ? metadata.getPackageName() : GENERATED_PACKAGE;
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        return javaFile.toString();
    }

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
                    if (rule.getParameters().containsKey("min")) {
                        builder.addMember("min", "$L", rule.getParameters().get("min"));
                    }
                    if (rule.getParameters().containsKey("max")) {
                        builder.addMember("max", "$L", rule.getParameters().get("max"));
                    }
                }
                break;
            case "Pattern":
                builder = AnnotationSpec.builder(Pattern.class);
                if (rule.getParameters() != null && rule.getParameters().containsKey("regexp")) {
                    builder.addMember("regexp", "$S", rule.getParameters().get("regexp"));
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

    private Class<?> getRelationshipAnnotation(String type) {
        switch (type) {
            case "OneToOne":
                return OneToOne.class;
            case "OneToMany":
                return OneToMany.class;
            case "ManyToOne":
                return ManyToOne.class;
            case "ManyToMany":
                return ManyToMany.class;
            default:
                throw new IllegalArgumentException("Unsupported relationship type: " + type);
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}