package org.dep.backend.dto;

public record ExamVenueSummary(
        Long id,
        String name,
        String address,
        String contactPhone,
        boolean hasSubject2Route,
        boolean hasSubject3Route
) {}
