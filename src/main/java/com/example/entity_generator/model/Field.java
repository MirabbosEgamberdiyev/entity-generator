package com.example.entity_generator.model;

import java.util.List;
import java.util.Map;

public class Field {
    private String name;
    private String type;
    private List<ValidationRule> validations;
    private SwaggerConfig swaggerConfig;
    private JsonConfig jsonConfig;
    private boolean primaryKey = false;
    private boolean nullable = true;
    private boolean unique = false;
    private String columnName;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private String defaultValue;

    // Constructors
    public Field() {}

    public Field(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ValidationRule> getValidations() {
        return validations;
    }

    public void setValidations(List<ValidationRule> validations) {
        this.validations = validations;
    }

    public SwaggerConfig getSwaggerConfig() {
        return swaggerConfig;
    }

    public void setSwaggerConfig(SwaggerConfig swaggerConfig) {
        this.swaggerConfig = swaggerConfig;
    }

    public JsonConfig getJsonConfig() {
        return jsonConfig;
    }

    public void setJsonConfig(JsonConfig jsonConfig) {
        this.jsonConfig = jsonConfig;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    // Inner classes for configuration
    public static class SwaggerConfig {
        private String description;
        private boolean required = false;
        private String format;
        private String example;
        private boolean hidden = false;
        private String pattern;
        private Integer minLength;
        private Integer maxLength;
        private Double minimum;
        private Double maximum;

        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        public Integer getMinLength() { return minLength; }
        public void setMinLength(Integer minLength) { this.minLength = minLength; }
        public Integer getMaxLength() { return maxLength; }
        public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
        public Double getMinimum() { return minimum; }
        public void setMinimum(Double minimum) { this.minimum = minimum; }
        public Double getMaximum() { return maximum; }
        public void setMaximum(Double maximum) { this.maximum = maximum; }
    }

    public static class JsonConfig {
        private String propertyName;
        private boolean ignore = false;
        private String format;
        private String pattern;
        private String timezone;
        private boolean writeOnly = false;
        private boolean readOnly = false;

        // Getters and Setters
        public String getPropertyName() { return propertyName; }
        public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
        public boolean isIgnore() { return ignore; }
        public void setIgnore(boolean ignore) { this.ignore = ignore; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public boolean isWriteOnly() { return writeOnly; }
        public void setWriteOnly(boolean writeOnly) { this.writeOnly = writeOnly; }
        public boolean isReadOnly() { return readOnly; }
        public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
    }
}