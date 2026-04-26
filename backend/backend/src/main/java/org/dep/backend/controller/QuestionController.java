package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.ExamResult;
import org.dep.backend.dto.ExamSubmitRequest;
import org.dep.backend.dto.WrongQuestionRequest;
import org.dep.backend.model.Question;
import org.dep.backend.model.WrongQuestion;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/questions")
    public List<Question> questions() {
        return questionService.findAll();
    }

    @GetMapping("/questions/random")
    public List<Question> randomQuestions(@RequestParam(defaultValue = "5") int count) {
        return questionService.findRandom(count);
    }

    @GetMapping("/wrong-questions")
    public List<WrongQuestion> wrongQuestions(HttpServletRequest request) {
        return questionService.findWrongQuestions(currentUser(request).id());
    }

    @PostMapping("/wrong-questions")
    public WrongQuestion addWrongQuestion(@RequestBody WrongQuestionRequest body, HttpServletRequest request) {
        return questionService.addWrongQuestion(currentUser(request).id(), body.questionId());
    }

    @DeleteMapping("/wrong-questions/{id}")
    public ResponseEntity<Void> deleteWrongQuestion(@PathVariable Long id, HttpServletRequest request) {
        questionService.deleteWrongQuestion(currentUser(request).id(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wrong-questions")
    public ResponseEntity<Void> clearWrongQuestions(HttpServletRequest request) {
        questionService.clearWrongQuestions(currentUser(request).id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exam/submit")
    public ExamResult submitExam(@RequestBody ExamSubmitRequest body, HttpServletRequest request) {
        return questionService.submitExam(currentUser(request).id(), body.answers());
    }

    private CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute("currentUser");
    }
}
