package org.dep.backend.dto;

public record ExamRegistrationAdminUpdate(
        String status,
        Integer score,
        String passed,
        String remark
) {}
