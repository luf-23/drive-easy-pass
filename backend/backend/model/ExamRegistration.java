package org.dep.backend.model;


import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExamRegistration(
        Long id,
        Long userId,
        Long scheduleId,
        Long venueId,
        String examType,
        LocalDate examDate,
        String status,
        Integer score,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {}
