package org.dep.backend.dto;

public record EnrollmentFunnelStatDto(
        String stage,
        Long count
) {
}
