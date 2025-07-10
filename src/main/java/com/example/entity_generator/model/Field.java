package com.example.entity_generator.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Field definition for an entity")
public class Field {
    @Schema(description = "Field name", example = "name")
    private String name;

    @Schema(description = "Field type", example = "String")
    private String type;

    @Schema(description = "Validation rules for the field")
    private List<ValidationRule> validations;

    @Schema(description = "Swagger configuration for the field")
    private SwaggerConfig swaggerConfig;

    @Schema(description = "JSON configuration for the field")
    private JsonConfig jsonConfig;

    @Schema(description = "Is the field a primary key", defaultValue = "false")
    private boolean primaryKey = false;

    @Schema(description = "Is the field nullable", defaultValue = "true")
    private boolean nullable = true;

    @Schema(description = "Is the field unique", defaultValue = "false")
    private boolean unique = false;

    @Schema(description = "Column name in the database", example = "name")
    private String columnName;

    @Schema(description = "Length of the field", example = "255")
    private Integer length;

    @Schema(description = "Precision for numeric fields")
    private Integer precision;

    @Schema(description = "Scale for numeric fields")
    private Integer scale;

    @Schema(description = "Default value for the field")
    private String defaultValue;

    @Data
    @NoArgsConstructor
    @Schema(description = "Swagger configuration for a field")
    public static class SwaggerConfig {
        @Schema(description = "Description of the field", example = "Product name")
        private String description;

        @Schema(description = "Is the field required", defaultValue = "false")
        private boolean required = false;

        @Schema(description = "Format of the field", example = "string")
        private String format;

        @Schema(description = "Example value for the field", example = "Laptop")
        private String example;

        @Schema(description = "Is the field hidden", defaultValue = "false")
        private boolean hidden = false;

        @Schema(description = "Pattern for the field", example = "[a-zA-Z]+")
        private String pattern;

        @Schema(description = "Minimum length of the field")
        private Integer minLength;

        @Schema(description = "Maximum length of the field")
        private Integer maxLength;

        @Schema(description = "Minimum value for numeric fields")
        private Double minimum;

        @Schema(description = "Maximum value for numeric fields")
        private Double maximum;
    }

    @Data
    @NoArgsConstructor
    @Schema(description = "JSON configuration for a field")
    public static class JsonConfig {
        @Schema(description = "JSON property name", example = "productName")
        private String propertyName;

        @Schema(description = "Ignore the field in JSON", defaultValue = "false")
        private boolean ignore = false;

        @Schema(description = "JSON format", example = "yyyy-MM-dd")
        private String format;

        @Schema(description = "Pattern for JSON serialization")
        private String pattern;

        @Schema(description = "Timezone for date fields")
        private String timezone;

        @Schema(description = "Write-only field", defaultValue = "false")
        private boolean writeOnly = false;

        @Schema(description = "Read-only field", defaultValue = "false")
        private boolean readOnly = false;
    }
}