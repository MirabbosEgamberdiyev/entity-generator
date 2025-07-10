package com.example.entity_generator.service;

import com.example.entity_generator.exception.EntityGenerationException;
import com.example.entity_generator.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ModelAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(ModelAnalyzerService.class);

    public EntityMetadata analyzeModel(String javaCode) throws EntityGenerationException {
        logger.info("Analyzing Java model code");
        EntityMetadata metadata = new EntityMetadata();

        metadata.setPackageName(extractPackageName(javaCode));
        metadata.setEntityName(extractClassName(javaCode));
        metadata.setFields(extractFields(javaCode));
        metadata.setRelationships(extractRelationships(javaCode));

        logger.info("Model analysis completed for entity: {}", metadata.getEntityName());
        return metadata;
    }

    private String extractPackageName(String javaCode) {
        Pattern packagePattern = Pattern.compile("package\\s+([\\w.]+);");
        Matcher matcher = packagePattern.matcher(javaCode);
        if (matcher.find()) {
            String packageName = matcher.group(1);
            logger.debug("Extracted package: {}", packageName);
            return packageName;
        }
        return null;
    }

    private String extractClassName(String javaCode) {
        Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(javaCode);
        if (matcher.find()) {
            String className = matcher.group(1);
            logger.debug("Extracted entity name: {}", className);
            return className;
        }
        throw new EntityGenerationException("Class name not found in Java code");
    }

    private List<Relationship> extractRelationships(String javaCode) {
        List<Relationship> relationships = new ArrayList<>();
        Pattern relPattern = Pattern.compile("@(OneToOne|OneToMany|ManyToOne|ManyToMany)\\s*\\((.*?)\\)");
        Matcher matcher = relPattern.matcher(javaCode);
        while (matcher.find()) {
            Relationship rel = new Relationship();
            rel.setType(matcher.group(1));
            String params = matcher.group(2);
            rel.setMappedBy(extractMappedBy(params));
            rel.setFetch(extractFetchType(params));
            relationships.add(rel);
            logger.debug("Extracted relationship: {}", rel);
        }
        return relationships;
    }

    private String extractMappedBy(String params) {
        Pattern mappedByPattern = Pattern.compile("mappedBy\\s*=\\s*\"(\\w+)\"");
        Matcher matcher = mappedByPattern.matcher(params);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractFetchType(String params) {
        Pattern fetchPattern = Pattern.compile("fetch\\s*=\\s*FetchType\\.(\\w+)");
        Matcher matcher = fetchPattern.matcher(params);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Map<String, Object> parseParameters(String params) {
        Map<String, Object> parameters = new HashMap<>();
        String[] paramArray = params.split(",");
        for (String param : paramArray) {
            String[] keyValue = param.trim().split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim().replace("\"", "");
                try {
                    if (value.matches("\\d+")) {
                        parameters.put(key, Integer.parseInt(value));
                    } else if (value.matches("\\d+\\.\\d+")) {
                        parameters.put(key, Double.parseDouble(value));
                    } else {
                        parameters.put(key, value);
                    }
                } catch (Exception e) {
                    parameters.put(key, value);
                    logger.warn("Failed to parse parameter: {} = {}", key, value);
                }
            }
        }
        return parameters;
    }

    private List<Field> extractFields(String javaCode) {
        List<Field> fields = new ArrayList<>();
        // Match fields with optional annotations, type, and name
        Pattern fieldPattern = Pattern.compile("(?:@\\w+\\s*(?:\\([^)]*\\))?\\s*)*" +
                "(private|protected|public)?\\s*(\\w+)\\s+(\\w+);");
        Matcher matcher = fieldPattern.matcher(javaCode);

        while (matcher.find()) {
            Field field = new Field();
            field.setType(matcher.group(2));
            field.setName(matcher.group(3));

            // Check for @Id annotation to mark primary key
            String fieldBlock = extractFieldBlock(javaCode, matcher.start());
            if (fieldBlock.contains("@Id")) {
                field.setPrimaryKey(true);
            }

            // Extract validations
            field.setValidations(extractValidations(fieldBlock, field.getName()));
            fields.add(field);
            logger.debug("Extracted field: {} of type {}, isPrimaryKey: {}",
                    field.getName(), field.getType(), field.isPrimaryKey());
        }
        return fields;
    }

    private String extractFieldBlock(String javaCode, int startIndex) {
        // Extract the block of code before the field declaration
        int endIndex = javaCode.indexOf(";", startIndex) + 1;
        int blockStart = javaCode.lastIndexOf("\n", startIndex);
        if (blockStart == -1) blockStart = 0;
        return javaCode.substring(blockStart, endIndex);
    }

    private List<ValidationRule> extractValidations(String fieldBlock, String fieldName) {
        List<ValidationRule> validations = new ArrayList<>();
        Pattern validationPattern = Pattern.compile("@(\\w+)\\s*(?:\\((.*?)\\))?");
        Matcher matcher = validationPattern.matcher(fieldBlock);

        while (matcher.find()) {
            String annotation = matcher.group(1);
            String params = matcher.group(2);
            // Only include known validation annotations
            if (isValidationAnnotation(annotation)) {
                ValidationRule rule = new ValidationRule(annotation, parseParameters(params != null ? params : ""));
                validations.add(rule);
                logger.debug("Extracted validation for field {}: {}", fieldName, rule);
            }
        }
        return validations;
    }

    private boolean isValidationAnnotation(String annotation) {
        return Set.of("NotNull", "NotBlank", "Size", "Pattern", "Min", "Max", "Email",
                "Positive", "Negative", "Digits").contains(annotation);
    }
}