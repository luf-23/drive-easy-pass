package org.dep.backend.dto;

public record AppRouteDto(
        Long id,
        String path,
        String name,
        String title,
        Long parentId,
        String redirect,
        String component,
        String icon,
        Integer rankNo,
        Boolean enabled
) {
}
