package org.dep.backend.util;

import java.util.Comparator;
import java.util.Locale;

public final class AnswerNormalizer {
    private AnswerNormalizer() {
    }

    public static String sortAnswerLetters(String letters) {
        if (letters == null || letters.isBlank()) {
            return "";
        }
        return letters.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .sorted(Comparator.naturalOrder())
                .reduce("", String::concat);
    }

    public static String normalizeAnswerKey(String answer) {
        if (answer == null) {
            return "";
        }
        String normalized = answer.trim().toUpperCase(Locale.ROOT).replace(",", "");
        return sortAnswerLetters(normalized);
    }
}
