package org.dep.backend.dto;

import java.util.List;

public record DrivingRouteResponse(
        List<double[]> path,
        int distanceMeters,
        int durationSeconds,
        String provider
) {}
