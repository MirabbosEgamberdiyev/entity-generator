package com.example.entity_generator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "Metadata for generating Entity and related components")
public class EntityMetadata {
    @Schema(description = "Name of the entity", example = "Product")
    private String entityName;

    @Schema(description = "Package name for generated classes", example = "com.example.generated")
    private String packageName = "com.example.generated";

    @Schema(description = "List of fields for the entity")
    private List<Field> fields;

    @Schema(description = "List of relationships for the entity")
    private List<Relationship> relationships;

    @Schema(description = "Swagger configuration for the entity")
    private Map<String, Object> swaggerConfig;

    @Schema(description = "Description of the entity", example = "Represents a product in the system")
    private String description;

    @Schema(description = "Enable validation annotations", defaultValue = "true")
    private boolean enableValidation = true;

    @Schema(description = "Enable Swagger annotations", defaultValue = "true")
    private boolean enableSwagger = true;

    @Schema(description = "Enable Jackson JSON annotations", defaultValue = "true")
    private boolean enableJsonAnnotations = true;
}