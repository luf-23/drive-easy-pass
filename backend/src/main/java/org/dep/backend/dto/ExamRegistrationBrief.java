package org.dep.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamRegistrationBrief(
        Long userId,
        String status,
        LocalDate examDate,
        LocalTime startTime
) {}
