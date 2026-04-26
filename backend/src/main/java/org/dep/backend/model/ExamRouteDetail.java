package org.dep.backend.model;

import java.time.LocalDateTime;

public record ExamRouteDetail(
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
        Integer sortOrder,
        LocalDateTime createTime
) {}
