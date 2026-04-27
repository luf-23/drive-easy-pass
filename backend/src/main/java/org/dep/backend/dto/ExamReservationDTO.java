package org.dep.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamReservationDTO(
        Long id,
        Long userId,
        String username,
        Long scheduleId,
        Long venueId,
        String venueName,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        String status,
        Integer score,
        String passed,
        String remark
) {}