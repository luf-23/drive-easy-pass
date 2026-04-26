package org.dep.backend.dto;

import java.math.BigDecimal;

public record ExamVenueDTO(
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
        String status
) {}
