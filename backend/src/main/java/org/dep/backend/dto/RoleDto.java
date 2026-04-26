package org.dep.backend.dto;

import java.util.List;

public record RoleDto(
        Long id,
        String code,
        String name,
        String description,
        Boolean enabled,
        List<Long> routeIds
) {
}
