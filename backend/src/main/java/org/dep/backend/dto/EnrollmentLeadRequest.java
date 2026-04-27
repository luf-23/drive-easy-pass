package org.dep.backend.dto;

public record EnrollmentLeadRequest(
        String name,
        String phone,
        Long sourceId,
        Long intentLevelId,
        Long statusId,
        Long ownerUserId,
        String nextFollowTime,
        String remark
) {
}
