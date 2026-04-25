package org.dep.backend.service;

import org.dep.backend.dto.AnswerRequest;
import org.dep.backend.dto.ExamResult;
import org.dep.backend.model.Question;
import org.dep.backend.model.WrongQuestion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Question> questionMapper = (rs, rowNum) -> new Question(
            rs.getLong("id"),
            rs.getString("content"),
            rs.getString("option_a"),
            rs.getString("option_b"),
            rs.getString("option_c"),
            rs.getString("option_d"),
            rs.getString("answer"),
            rs.getString("explanation")
    );

    private final RowMapper<WrongQuestion> wrongQuestionMapper = (rs, rowNum) -> new WrongQuestion(
            rs.getLong("wrong_id"),
            rs.getLong("user_id"),
            new Question(
                    rs.getLong("question_id"),
                    rs.getString("content"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("answer"),
                    rs.getString("explanation")
            ),
            rs.getTimestamp("create_time").toLocalDateTime()
    );

    public QuestionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Question> findAll() {
        return jdbcTemplate.query("""
                SELECT id, content, option_a, option_b, option_c, option_d, answer, explanation
                FROM questions
                ORDER BY id
                """, questionMapper);
    }

    public List<Question> findRandom(int count) {
        int safeCount = Math.max(count, 1);
        return jdbcTemplate.query("""
                SELECT id, content, option_a, option_b, option_c, option_d, answer, explanation
                FROM questions
                ORDER BY RAND()
                LIMIT ?
                """, questionMapper, safeCount);
    }

    public List<WrongQuestion> findWrongQuestions(Long userId) {
        return jdbcTemplate.query(wrongQuestionSql() + " WHERE w.user_id = ? ORDER BY w.create_time DESC", wrongQuestionMapper, userId);
    }

    public WrongQuestion addWrongQuestion(Long userId, Long questionId) {
        findQuestion(questionId);
        jdbcTemplate.update("""
                INSERT IGNORE INTO wrong_questions (user_id, question_id, create_time)
                VALUES (?, ?, NOW())
                """, userId, questionId);

        return jdbcTemplate.queryForObject(
                wrongQuestionSql() + " WHERE w.user_id = ? AND w.question_id = ?",
                wrongQuestionMapper,
                userId,
                questionId
        );
    }

    public void deleteWrongQuestion(Long userId, Long id) {
        jdbcTemplate.update("DELETE FROM wrong_questions WHERE id = ? AND user_id = ?", id, userId);
    }

    public void clearWrongQuestions(Long userId) {
        jdbcTemplate.update("DELETE FROM wrong_questions WHERE user_id = ?", userId);
    }

    public ExamResult submitExam(Long userId, List<AnswerRequest> answers) {
        List<Question> wrong = new ArrayList<>();
        int correct = 0;

        for (AnswerRequest answer : answers) {
            Question question = findQuestion(answer.questionId());
            if (question.answer().equalsIgnoreCase(normalizeAnswer(answer.answer()))) {
                correct++;
            } else {
                wrong.add(question);
                addWrongQuestion(userId, question.id());
            }
        }

        int total = answers.size();
        int score = total == 0 ? 0 : Math.round(correct * 100f / total);
        return new ExamResult(total, correct, score, wrong);
    }

    private Question findQuestion(Long questionId) {
        List<Question> questions = jdbcTemplate.query("""
                SELECT id, content, option_a, option_b, option_c, option_d, answer, explanation
                FROM questions
                WHERE id = ?
                """, questionMapper, questionId);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        return questions.getFirst();
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().toUpperCase();
    }

    private String wrongQuestionSql() {
        return """
                SELECT
                  w.id AS wrong_id,
                  w.user_id,
                  w.create_time,
                  q.id AS question_id,
                  q.content,
                  q.option_a,
                  q.option_b,
                  q.option_c,
                  q.option_d,
                  q.answer,
                  q.explanation
                FROM wrong_questions w
                JOIN questions q ON q.id = w.question_id
                """;
    }
}
