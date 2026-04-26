package org.dep.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamRegistrationDTO(
        Long id,
        Long scheduleId,
        Long venueId,
        String venueName,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        Integer score,
        int availableSlots
) {}
