package org.dep.backend.dto;

public record PublicEnrollmentIntentRequest(
        String name,
        String phone,
        String vehicleType,
        String classType,
        String source,
        String remark
) {
}
