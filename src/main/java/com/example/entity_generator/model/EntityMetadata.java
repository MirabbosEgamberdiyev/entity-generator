package com.example.entity_generator.model;


import java.util.List;
import java.util.Map;

public class EntityMetadata {
    private String entityName;
    private String packageName;
    private List<Field> fields;
    private List<Relationship> relationships;
    private Map<String, Object> swaggerConfig;
    private String description;
    private boolean enableValidation = true;
    private boolean enableSwagger = true;
    private boolean enableJsonAnnotations = true;

    // Constructors
    public EntityMetadata() {}

    public EntityMetadata(String entityName, List<Field> fields) {
        this.entityName = entityName;
        this.fields = fields;
    }

    // Getters and Setters
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPackageName() {
        return packageName != null ? packageName : "com.example.generated";
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public Map<String, Object> getSwaggerConfig() {
        return swaggerConfig;
    }

    public void setSwaggerConfig(Map<String, Object> swaggerConfig) {
        this.swaggerConfig = swaggerConfig;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }

    public boolean isEnableSwagger() {
        return enableSwagger;
    }

    public void setEnableSwagger(boolean enableSwagger) {
        this.enableSwagger = enableSwagger;
    }

    public boolean isEnableJsonAnnotations() {
        return enableJsonAnnotations;
    }

    public void setEnableJsonAnnotations(boolean enableJsonAnnotations) {
        this.enableJsonAnnotations = enableJsonAnnotations;
    }
}