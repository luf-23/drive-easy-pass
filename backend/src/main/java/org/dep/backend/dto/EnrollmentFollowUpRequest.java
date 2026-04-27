package org.dep.backend.dto;

public record EnrollmentFollowUpRequest(
        String content,
        Long followTypeId,
        String nextFollowTime
) {
}
