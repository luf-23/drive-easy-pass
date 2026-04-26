package org.dep.backend.dto;

public record ExamRouteDTO(
        Long id,
        Long venueId,
        String routeName,
        String routeNumber,
        String description,
        String startPoint,
        String endPoint,
        Integer distance,
        String difficulty,
        String points,
        String mapImageUrl,
        Integer sortOrder
) {}
