package org.dep.backend.mapper.projection;

import java.time.LocalDateTime;

public record WrongQuestionRow(
        Long wrongId,
        Long userId,
        LocalDateTime createTime,
        Long questionId,
        String content,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String answer,
        String explanation
) {
}
