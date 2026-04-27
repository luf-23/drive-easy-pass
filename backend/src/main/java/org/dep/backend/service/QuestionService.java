package org.dep.backend.service;

import org.dep.backend.dto.AnswerRequest;
import org.dep.backend.dto.ExamResult;
import org.dep.backend.mapper.QuestionMapper;
import org.dep.backend.mapper.projection.WrongQuestionRow;
import org.dep.backend.model.Question;
import org.dep.backend.model.WrongQuestion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {
    private static final int QUESTION_SCORE = 5;

    private final QuestionMapper questionMapper;
    private volatile Boolean hasExamTypeColumn;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<Question> findAll(String examType) {
        if (examType != null && !examType.isBlank() && ensureExamTypeColumn()) {
            return questionMapper.findAllByExamType(examType);
        }
        return questionMapper.findAll();
    }

    public List<Question> findRandom(int count, String examType) {
        int safeCount = Math.max(count, 1);
        if (examType != null && !examType.isBlank() && ensureExamTypeColumn()) {
            List<Question> filtered = questionMapper.findRandomByExamType(examType, safeCount);

            // Fallback to all questions when selected exam type has no seeded data.
            if (!filtered.isEmpty()) {
                return filtered;
            }
        }
        return questionMapper.findRandom(safeCount);
    }

    public List<WrongQuestion> findWrongQuestions(Long userId) {
        return questionMapper.findWrongQuestionsByUserId(userId).stream().map(this::toWrongQuestion).toList();
    }

    public WrongQuestion addWrongQuestion(Long userId, Long questionId) {
        findQuestion(questionId);
        questionMapper.insertWrongQuestion(userId, questionId);
        WrongQuestionRow row = questionMapper.findWrongQuestionByUserAndQuestion(userId, questionId);
        return toWrongQuestion(row);
    }

    public void deleteWrongQuestion(Long userId, Long id) {
        questionMapper.deleteWrongQuestion(userId, id);
    }

    public void clearWrongQuestions(Long userId) {
        questionMapper.clearWrongQuestions(userId);
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
        int score = correct * QUESTION_SCORE;
        return new ExamResult(total, correct, score, wrong);
    }

    private Question findQuestion(Long questionId) {
        Question question = questionMapper.findQuestionById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        return question;
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().toUpperCase();
    }

    private boolean ensureExamTypeColumn() {
        if (hasExamTypeColumn != null) {
            return hasExamTypeColumn;
        }

        synchronized (this) {
            if (hasExamTypeColumn != null) {
                return hasExamTypeColumn;
            }

            Integer count = questionMapper.countExamTypeColumn();

            if (count != null && count > 0) {
                hasExamTypeColumn = true;
                return true;
            }

            questionMapper.addExamTypeColumn();
            hasExamTypeColumn = true;
            return true;
        }
    }

    private WrongQuestion toWrongQuestion(WrongQuestionRow row) {
        if (row == null) {
            throw new IllegalArgumentException("Wrong question not found");
        }
        return new WrongQuestion(
                row.wrongId(),
                row.userId(),
                new Question(
                        row.questionId(),
                        row.content(),
                        row.optionA(),
                        row.optionB(),
                        row.optionC(),
                        row.optionD(),
                        row.answer(),
                        row.explanation()
                ),
                row.createTime()
        );
    }
}
