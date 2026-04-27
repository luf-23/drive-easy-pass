package org.dep.backend.dto;

public record EnrollmentOwnerPerformanceDto(
        Long ownerUserId,
        String ownerName,
        Long signedCount
) {
}
