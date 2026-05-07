package org.dep.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleCard(
        Long id,
        Long venueId,
        String venueName,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        int bookedCount,
        int availableSlots,
        String remark
) {}
