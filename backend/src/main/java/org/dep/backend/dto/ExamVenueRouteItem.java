package org.dep.backend.dto;

public record ExamVenueRouteItem(
        String examType,
        String mediaType,
        String title,
        String routeUrl,
        String routePath,
        String coverUrl,
        String remark,
        Boolean enabled
) {}
