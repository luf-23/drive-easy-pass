package org.dep.backend.dto;

import java.time.LocalDateTime;

public record EnrollmentFollowUpDto(
        Long id,
        Long leadId,
        String content,
        String followType,
        LocalDateTime nextFollowTime,
        Long creatorUserId,
        String creatorName,
        LocalDateTime createTime
) {
}
