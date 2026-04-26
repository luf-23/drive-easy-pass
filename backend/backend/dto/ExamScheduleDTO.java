package org.dep.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleDTO(
        Long id,
        Long venueId,
        String venueName,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        String examType,
        Integer totalSlots,
        Integer reservedSlots,
        Integer availableSlots,
        String status
) {}
