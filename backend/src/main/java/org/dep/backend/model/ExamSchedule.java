package org.dep.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record ExamSchedule(
        Long id,
        Long venueId,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        String examType,
        Integer totalSlots,
        Integer reservedSlots,
        Integer availableSlots,
        String status,
        LocalDateTime createTime
) {}