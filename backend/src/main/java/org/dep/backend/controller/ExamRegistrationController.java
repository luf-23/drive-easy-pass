package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.ExamRegistrationApplyRequest;
import org.dep.backend.dto.ExamRegistrationDTO;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.ExamRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/exam-registrations")
public class ExamRegistrationController {
    private final ExamRegistrationService examRegistrationService;

    public ExamRegistrationController(ExamRegistrationService examRegistrationService) {
        this.examRegistrationService = examRegistrationService;
    }

    @GetMapping("/mine")
    public List<ExamRegistrationDTO> mine(HttpServletRequest request) {
        return examRegistrationService.listMine(currentUser(request).id());
    }

    @PostMapping
    public ExamRegistrationDTO apply(@RequestBody ExamRegistrationApplyRequest body, HttpServletRequest request) {
        return examRegistrationService.apply(currentUser(request).id(), body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, HttpServletRequest request) {
        examRegistrationService.cancel(currentUser(request).id(), id);
        return ResponseEntity.noContent().build();
    }

    private CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute("currentUser");
    }
}
