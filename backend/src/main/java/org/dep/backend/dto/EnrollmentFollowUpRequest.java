package org.dep.backend.dto;

public record EnrollmentFollowUpRequest(
        String content,
        String followType,
        String nextFollowTime
) {
}
