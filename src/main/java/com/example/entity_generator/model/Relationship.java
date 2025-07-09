package com.example.entity_generator.model;

import java.util.List;

public class Relationship {
    private String type; // OneToOne, OneToMany, ManyToOne, ManyToMany
    private String sourceField;
    private String targetEntity;
    private String targetField;
    private String mappedBy;
    private List<String> cascade;
    private String fetch; // LAZY, EAGER
    private boolean optional = true;
    private JoinColumnConfig joinColumn;
    private JoinTableConfig joinTable;

    // Constructors
    public Relationship() {}

    public Relationship(String type, String sourceField, String targetEntity) {
        this.type = type;
        this.sourceField = sourceField;
        this.targetEntity = targetEntity;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    public List<String> getCascade() {
        return cascade;
    }

    public void setCascade(List<String> cascade) {
        this.cascade = cascade;
    }

    public String getFetch() {
        return fetch;
    }

    public void setFetch(String fetch) {
        this.fetch = fetch;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public JoinColumnConfig getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(JoinColumnConfig joinColumn) {
        this.joinColumn = joinColumn;
    }

    public JoinTableConfig getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(JoinTableConfig joinTable) {
        this.joinTable = joinTable;
    }

    // Inner classes for configuration
    public static class JoinColumnConfig {
        private String name;
        private String referencedColumnName;
        private boolean nullable = true;
        private boolean unique = false;
        private String foreignKey;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getReferencedColumnName() { return referencedColumnName; }
        public void setReferencedColumnName(String referencedColumnName) { this.referencedColumnName = referencedColumnName; }
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        public boolean isUnique() { return unique; }
        public void setUnique(boolean unique) { this.unique = unique; }
        public String getForeignKey() { return foreignKey; }
        public void setForeignKey(String foreignKey) { this.foreignKey = foreignKey; }
    }

    public static class JoinTableConfig {
        private String name;
        private List<JoinColumnConfig> joinColumns;
        private List<JoinColumnConfig> inverseJoinColumns;
        private String schema;
        private String catalog;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<JoinColumnConfig> getJoinColumns() { return joinColumns; }
        public void setJoinColumns(List<JoinColumnConfig> joinColumns) { this.joinColumns = joinColumns; }
        public List<JoinColumnConfig> getInverseJoinColumns() { return inverseJoinColumns; }
        public void setInverseJoinColumns(List<JoinColumnConfig> inverseJoinColumns) { this.inverseJoinColumns = inverseJoinColumns; }
        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
        public String getCatalog() { return catalog; }
        public void setCatalog(String catalog) { this.catalog = catalog; }
    }
}