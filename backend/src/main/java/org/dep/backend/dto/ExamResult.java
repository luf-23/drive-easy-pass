package org.dep.backend.dto;

import org.dep.backend.model.Question;

import java.util.List;

public record ExamResult(
        int total,
        int correct,
        int score,
        List<Question> wrongQuestions
) {
}
