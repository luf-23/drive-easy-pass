package org.dep.backend.mapper.projection;

public record RoleBaseRecord(
        Long id,
        String code,
        String name,
        String description,
        Boolean enabled
) {
}
