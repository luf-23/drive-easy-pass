package org.dep.backend.dto;

public record AppRouteRequest(
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
