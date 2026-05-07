package org.dep.backend.controller;

import org.dep.backend.dto.ExamScheduleCard;
import org.dep.backend.service.ExamRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/exam-schedules")
public class PublicExamScheduleController {
    private final ExamRegistrationService examRegistrationService;

    public PublicExamScheduleController(ExamRegistrationService examRegistrationService) {
        this.examRegistrationService = examRegistrationService;
    }

    @GetMapping
    public List<ExamScheduleCard> list(@RequestParam(required = false) String examType) {
        return examRegistrationService.listPublicSchedules(examType);
    }
}
