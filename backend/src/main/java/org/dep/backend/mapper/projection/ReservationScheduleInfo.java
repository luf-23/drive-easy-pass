package org.dep.backend.mapper.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationScheduleInfo(
        Long venueId,
        String examType,
        LocalDate examDate,
        LocalTime startTime,
        Integer availableSlots,
        String status,
        String venueName
) {
}
