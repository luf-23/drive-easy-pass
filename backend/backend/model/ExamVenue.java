package org.dep.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExamVenue(
        Long id,
        String venueName,
        String venueCode,
        String address,
        String district,
        String contactPhone,
        String examType,
        Integer totalSlots,
        Integer availableSlots,
        String routeDescription,
        String routeMapUrl,
        String facilities,
        String businessHours,
        BigDecimal longitude,
        BigDecimal latitude,
        String status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {}
