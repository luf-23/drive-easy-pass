package org.dep.backend.model;

public record LeadSource(
        Long id,
        String code,
        String name,
        String description,
        Boolean enabled
) {
}