package org.dep.backend.dto;

public record EnrollmentLeadRequest(
        String name,
        String phone,
        String source,
        String intentLevel,
        String status,
        Long ownerUserId,
        String nextFollowTime,
        String remark
) {
}
