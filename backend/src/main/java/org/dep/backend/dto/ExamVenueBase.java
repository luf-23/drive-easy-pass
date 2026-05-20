package org.dep.backend.dto;

public record ExamVenueBase(
        Long id,
        String name,
        String address,
        String contactPhone
) {}
