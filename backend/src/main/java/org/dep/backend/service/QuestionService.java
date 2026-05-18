package org.dep.backend.service;

import org.dep.backend.dto.AnswerRequest;
import org.dep.backend.dto.ExamResult;
import org.dep.backend.mapper.QuestionMapper;
import org.dep.backend.mapper.projection.WrongQuestionRow;
import org.dep.backend.model.Question;
import org.dep.backend.model.WrongQuestion;
import org.dep.backend.practice.CarQuestionRepository;
import org.dep.backend.util.AnswerNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class QuestionService {
    private static final int QUESTION_SCORE = 5;

    private final QuestionMapper questionMapper;
    private final CarQuestionRepository carQuestionRepository;
    private volatile Boolean hasExamTypeColumn;

    public QuestionService(QuestionMapper questionMapper, CarQuestionRepository carQuestionRepository) {
        this.questionMapper = questionMapper;
        this.carQuestionRepository = carQuestionRepository;
    }

    public List<Question> findAll(String examType) {
        List<Question> fromCar = findAllFromCar(examType);
        if (!fromCar.isEmpty()) {
            return fromCar;
        }
        return findAllFromMock(examType);
    }

    public List<Question> findRandom(int count, String examType) {
        int safeCount = Math.max(count, 1);
        List<Question> fromCar = findRandomFromCar(examType, safeCount);
        if (!fromCar.isEmpty()) {
            return fromCar;
        }
        return findRandomFromMock(examType, safeCount);
    }

    public List<WrongQuestion> findWrongQuestions(Long userId) {
        return questionMapper.findWrongQuestionsByUserId(userId).stream().map(this::toWrongQuestion).toList();
    }

    public WrongQuestion addWrongQuestion(Long userId, Long questionId) {
        Question question = findQuestion(questionId);
        ensureQuestionStored(question, resolveExamType(questionId));
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
            if (isAnswerCorrect(answer.answer(), question.answer())) {
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

    private List<Question> findAllFromCar(String examType) {
        if (!carQuestionRepository.isAvailable()) {
            return List.of();
        }

        if (examType != null && !examType.isBlank()) {
            return carQuestionRepository.findAll(examType);
        }

        List<Question> combined = new ArrayList<>();
        combined.addAll(carQuestionRepository.findAll("科目一"));
        combined.addAll(carQuestionRepository.findAll("科目四"));
        return combined;
    }

    private List<Question> findRandomFromCar(String examType, int count) {
        if (!carQuestionRepository.isAvailable() || examType == null || examType.isBlank()) {
            return List.of();
        }
        return carQuestionRepository.findRandom(examType, count);
    }

    private List<Question> findAllFromMock(String examType) {
        if (examType != null && !examType.isBlank() && ensureExamTypeColumn()) {
            return questionMapper.findAllByExamType(examType);
        }
        return questionMapper.findAll();
    }

    private List<Question> findRandomFromMock(String examType, int count) {
        if (examType != null && !examType.isBlank() && ensureExamTypeColumn()) {
            List<Question> filtered = questionMapper.findRandomByExamType(examType, count);
            if (!filtered.isEmpty()) {
                return filtered;
            }
        }
        return questionMapper.findRandom(count);
    }

    private Question findQuestion(Long questionId) {
        if (carQuestionRepository.isCarQuestionId(questionId)) {
            return carQuestionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        }

        Question question = questionMapper.findQuestionById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        return question;
    }

    private void ensureQuestionStored(Question question, String examType) {
        if (questionMapper.findQuestionById(question.id()) != null) {
            return;
        }

        ensureExamTypeColumn();
        questionMapper.insertIgnore(
                question.id(),
                question.content(),
                question.optionA(),
                question.optionB(),
                question.optionC(),
                question.optionD(),
                examType,
                question.answer(),
                question.explanation()
        );
    }

    private String resolveExamType(Long questionId) {
        String examType = carQuestionRepository.examTypeForQuestionId(questionId);
        return examType == null ? "科目一" : examType;
    }

    private boolean isAnswerCorrect(String userAnswer, String correctAnswer) {
        return normalizeAnswerKey(userAnswer).equals(normalizeAnswerKey(correctAnswer));
    }

    private String normalizeAnswerKey(String answer) {
        return AnswerNormalizer.normalizeAnswerKey(answer);
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
