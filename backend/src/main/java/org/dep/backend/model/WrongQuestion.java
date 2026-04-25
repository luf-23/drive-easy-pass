package org.dep.backend.model;

import java.time.LocalDateTime;

public record WrongQuestion(
        Long id,
        Long userId,
        Question question,
        LocalDateTime createTime
) {
}
