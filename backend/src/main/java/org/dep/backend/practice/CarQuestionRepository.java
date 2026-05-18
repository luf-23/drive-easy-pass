package org.dep.backend.practice;

import jakarta.annotation.PostConstruct;
import org.dep.backend.model.Question;
import org.dep.backend.util.AnswerNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class CarQuestionRepository {
    private static final Logger log = LoggerFactory.getLogger(CarQuestionRepository.class);

    public static final long SUBJECT1_ID_OFFSET = 1_000_000L;
    public static final long SUBJECT4_ID_OFFSET = 2_000_000L;

    private static final Map<String, String> EXAM_TYPE_TABLE = Map.of(
            "科目一", "c1_1",
            "科目四", "c1_4"
    );

    private final PracticeDbProperties properties;
    private volatile boolean available;
    private volatile Path databasePath;

    public CarQuestionRepository(PracticeDbProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        try {
            databasePath = resolveDatabasePath(properties.dbPath());
            available = databasePath != null && Files.isRegularFile(databasePath);
            if (available) {
                log.info("Practice question bank loaded from {}", databasePath);
            } else {
                log.warn("Practice question bank not found at {}, will use MySQL mock data", properties.dbPath());
            }
        } catch (Exception ex) {
            available = false;
            log.warn("Failed to initialize practice question bank: {}", ex.getMessage());
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public List<Question> findAll(String examType) {
        String table = tableForExamType(examType);
        if (table == null) {
            return List.of();
        }
        return query(table, "SELECT id, question, item1, item2, item3, item4, answer, explains FROM " + table + " ORDER BY CAST(id AS INTEGER)");
    }

    public List<Question> findRandom(String examType, int count) {
        String table = tableForExamType(examType);
        if (table == null) {
            return List.of();
        }
        int safeCount = Math.max(count, 1);
        return query(
                table,
                "SELECT id, question, item1, item2, item3, item4, answer, explains FROM "
                        + table
                        + " ORDER BY RANDOM() LIMIT ?",
                safeCount
        );
    }

    public Optional<Question> findById(long questionId) {
        if (questionId >= SUBJECT4_ID_OFFSET) {
            return findByTableAndRawId("c1_4", questionId - SUBJECT4_ID_OFFSET);
        }
        if (questionId >= SUBJECT1_ID_OFFSET) {
            return findByTableAndRawId("c1_1", questionId - SUBJECT1_ID_OFFSET);
        }
        return Optional.empty();
    }

    public String examTypeForQuestionId(long questionId) {
        if (questionId >= SUBJECT4_ID_OFFSET) {
            return "科目四";
        }
        if (questionId >= SUBJECT1_ID_OFFSET) {
            return "科目一";
        }
        return null;
    }

    public boolean isCarQuestionId(long questionId) {
        return questionId >= SUBJECT1_ID_OFFSET;
    }

    private Optional<Question> findByTableAndRawId(String table, long rawId) {
        List<Question> rows = query(
                table,
                "SELECT id, question, item1, item2, item3, item4, answer, explains FROM "
                        + table
                        + " WHERE id = ?",
                String.valueOf(rawId)
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private List<Question> query(String table, String sql, Object... params) {
        if (!available) {
            return List.of();
        }

        List<Question> result = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer intParam) {
                    statement.setInt(i + 1, intParam);
                } else {
                    statement.setString(i + 1, String.valueOf(param));
                }
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(table, rs));
                }
            }
        } catch (SQLException ex) {
            log.warn("Failed to query practice bank table {}: {}", table, ex.getMessage());
            return List.of();
        }
        return result;
    }

    private Question mapRow(String table, ResultSet rs) throws SQLException {
        String rawId = rs.getString("id");
        long globalId = toGlobalId(table, rawId);
        return new Question(
                globalId,
                nullToEmpty(rs.getString("question")),
                nullToEmpty(rs.getString("item1")),
                nullToEmpty(rs.getString("item2")),
                nullToEmpty(rs.getString("item3")),
                nullToEmpty(rs.getString("item4")),
                convertCarAnswer(rs.getString("answer")),
                nullToEmpty(rs.getString("explains"))
        );
    }

    static long toGlobalId(String table, String rawId) {
        long numericId = Long.parseLong(rawId.trim());
        long offset = "c1_4".equals(table) ? SUBJECT4_ID_OFFSET : SUBJECT1_ID_OFFSET;
        return offset + numericId;
    }

    static String convertCarAnswer(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String value = raw.trim();
        if (value.matches("[1-4]+")) {
            if (value.length() == 1) {
                return optionLetter(value.charAt(0));
            }
            StringBuilder letters = new StringBuilder();
            for (char digit : value.toCharArray()) {
                if (digit >= '1' && digit <= '4') {
                    letters.append(optionLetter(digit));
                }
            }
            return AnswerNormalizer.sortAnswerLetters(letters.toString());
        }

        try {
            int bitmask = Integer.parseInt(value);
            if (bitmask >= 1 && bitmask <= 4) {
                return optionLetter((char) ('0' + bitmask));
            }
            StringBuilder letters = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if ((bitmask & (1 << i)) != 0) {
                    letters.append((char) ('A' + i));
                }
            }
            if (!letters.isEmpty()) {
                return AnswerNormalizer.sortAnswerLetters(letters.toString());
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }

        return value.toUpperCase(Locale.ROOT);
    }

    private static String optionLetter(char digit) {
        return String.valueOf((char) ('A' + (digit - '1')));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String tableForExamType(String examType) {
        if (examType == null || examType.isBlank()) {
            return null;
        }
        return EXAM_TYPE_TABLE.get(examType.trim());
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
    }

    private Path resolveDatabasePath(String configuredPath) throws IOException {
        if (configuredPath == null || configuredPath.isBlank()) {
            return null;
        }

        if (configuredPath.startsWith("classpath:")) {
            String resourcePath = configuredPath.substring("classpath:".length());
            Resource resource = new org.springframework.core.io.ClassPathResource(resourcePath);
            if (!resource.exists()) {
                return null;
            }
            Path tempFile = Files.createTempFile("car-practice-", ".db");
            tempFile.toFile().deleteOnExit();
            try (InputStream input = resource.getInputStream()) {
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        }

        return Path.of(configuredPath);
    }
}
