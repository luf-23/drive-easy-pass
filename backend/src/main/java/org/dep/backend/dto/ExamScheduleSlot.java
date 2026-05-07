package org.dep.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleSlot(
        Long id,
        Long venueId,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        int capacity,
        String remark
) {}
