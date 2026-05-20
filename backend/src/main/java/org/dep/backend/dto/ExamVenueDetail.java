package org.dep.backend.dto;

import java.util.List;

public record ExamVenueDetail(
        Long id,
        String name,
        String address,
        String contactPhone,
        List<ExamVenueRouteItem> routes
) {}
