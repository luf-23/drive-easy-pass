package org.dep.backend.dto;

import java.time.LocalDateTime;

public record EnrollmentLeadDto(
        Long id,
        String name,
        String phone,
        Long sourceId,
        String sourceName,
        Long intentLevelId,
        String intentLevelName,
        Long statusId,
        String statusName,
        Long ownerUserId,
        String ownerName,
        LocalDateTime nextFollowTime,
        String remark,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
