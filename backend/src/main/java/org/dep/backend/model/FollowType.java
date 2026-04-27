package org.dep.backend.model;

public record FollowType(
        Long id,
        String code,
        String name,
        String description,
        Boolean enabled
) {
}