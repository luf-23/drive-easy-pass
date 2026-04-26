package org.dep.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record ExamReservation(
        Long id,
        Long userId,
        Long scheduleId,
        Long venueId,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        String status,
        Integer score,
        String passed,
        String remark,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {}
