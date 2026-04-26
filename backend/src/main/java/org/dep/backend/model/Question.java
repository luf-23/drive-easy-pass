package org.dep.backend.model;

public record Question(
        Long id,
        String content,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String answer,
        String explanation
) {
}
