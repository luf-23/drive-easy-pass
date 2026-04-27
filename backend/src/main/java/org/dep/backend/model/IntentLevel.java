package org.dep.backend.model;

public record IntentLevel(
        Long id,
        String code,
        String name,
        String description,
        Integer priority,
        Boolean enabled
) {
}