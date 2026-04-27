package org.dep.backend.model;

public record LeadStatus(
        Long id,
        String code,
        String name,
        String description,
        Boolean isFinal,
        Integer sortOrder,
        Boolean enabled
) {
}