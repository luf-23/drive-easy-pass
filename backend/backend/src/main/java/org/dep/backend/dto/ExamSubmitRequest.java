package org.dep.backend.dto;

import java.util.List;

public record ExamSubmitRequest(List<AnswerRequest> answers) {
}
