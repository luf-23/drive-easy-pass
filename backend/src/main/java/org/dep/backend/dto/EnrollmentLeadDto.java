package org.dep.backend.dto;

import java.time.LocalDateTime;

public record EnrollmentLeadDto(
        Long id,
        String name,
        String phone,
        String source,
        String intentLevel,
        String status,
        Long ownerUserId,
        String ownerName,
        LocalDateTime nextFollowTime,
        String remark,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
