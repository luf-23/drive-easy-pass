package org.dep.backend.dto;

public record ExamScoreRequest(
        Long reservationId,
        int score
) {}
