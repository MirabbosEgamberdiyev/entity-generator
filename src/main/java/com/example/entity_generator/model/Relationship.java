package com.example.entity_generator.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Relationship {
    private String type; // OneToOne, OneToMany, ManyToOne, ManyToMany
    private String sourceField;
    private String targetEntity;
    private String mappedBy;
    private String fetch; // EAGER, LAZY
    private List<String> cascade;
    private boolean optional = true;
    private String joinColumn;
    private String inverseJoinColumn;
    private String joinTable;
}