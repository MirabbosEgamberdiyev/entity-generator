package com.example.entity_generator.service;


import com.example.entity_generator.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ModelAnalyzerService {

    public EntityMetadata analyzeModel(String javaCode) {
        EntityMetadata metadata = new EntityMetadata();

        // Extract package name
        Pattern packagePattern = Pattern.compile("package\\s+([\\w.]+);");
        Matcher packageMatcher = packagePattern.matcher(javaCode);
        if (packageMatcher.find()) {
            metadata.setPackageName(packageMatcher.group(1));
        }

        // Extract class name
        Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(javaCode);
        if (classMatcher.find()) {
            metadata.setEntityName(classMatcher.group(1));
        }

        // Extract fields
        List<Field> fields = new ArrayList<>();
        Pattern fieldPattern = Pattern.compile("(private|protected|public)?\\s*(\\w+)\\s+(\\w+);");
        Matcher fieldMatcher = fieldPattern.matcher(javaCode);
        while (fieldMatcher.find()) {
            Field field = new Field();
            field.setType(fieldMatcher.group(2));
            field.setName(fieldMatcher.group(3));
            fields.add(field);
        }

        // Extract validations
        for (Field field : fields) {
            List<ValidationRule> validations = new ArrayList<>();
            Pattern validationPattern = Pattern.compile("@(\\w+)\\s*\\((.*?)\\)");
            Matcher validationMatcher = validationPattern.matcher(javaCode);
            while (validationMatcher.find()) {
                String annotation = validationMatcher.group(1);
                String params = validationMatcher.group(2);
                ValidationRule rule = new ValidationRule(annotation);
                if (!params.isEmpty()) {
                    rule.setParameters(parseParameters(params));
                }
                validations.add(rule);
            }
            field.setValidations(validations);
        }

        // Extract relationships
        List<Relationship> relationships = new ArrayList<>();
        Pattern relPattern = Pattern.compile("@(OneToOne|OneToMany|ManyToOne|ManyToMany)\\s*\\((.*?)\\)");
        Matcher relMatcher = relPattern.matcher(javaCode);
        while (relMatcher.find()) {
            Relationship rel = new Relationship();
            rel.setType(relMatcher.group(1));
            String params = relMatcher.group(2);
            if (params.contains("mappedBy")) {
                Pattern mappedByPattern = Pattern.compile("mappedBy\\s*=\\s*\"(\\w+)\"");
                Matcher mappedByMatcher = mappedByPattern.matcher(params);
                if (mappedByMatcher.find()) {
                    rel.setMappedBy(mappedByMatcher.group(1));
                }
            }
            if (params.contains("fetch")) {
                Pattern fetchPattern = Pattern.compile("fetch\\s*=\\s*FetchType\\.(\\w+)");
                Matcher fetchMatcher = fetchPattern.matcher(params);
                if (fetchMatcher.find()) {
                    rel.setFetch(fetchMatcher.group(1));
                }
            }
            relationships.add(rel);
        }

        metadata.setFields(fields);
        metadata.setRelationships(relationships);
        return metadata;
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
                }
            }
        }
        return parameters;
    }
}