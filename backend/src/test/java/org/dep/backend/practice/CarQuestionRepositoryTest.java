package org.dep.backend.practice;

import org.dep.backend.util.AnswerNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarQuestionRepositoryTest {

    @Test
    void convertCarAnswer_mapsSingleAndMultiOptions() {
        assertEquals("C", CarQuestionRepository.convertCarAnswer("3"));
        assertEquals("AC", CarQuestionRepository.convertCarAnswer("13"));
        assertEquals("D", CarQuestionRepository.convertCarAnswer("8"));
    }

    @Test
    void sortAnswerLetters_ordersLetters() {
        assertEquals("AC", AnswerNormalizer.sortAnswerLetters("CA"));
    }
}
