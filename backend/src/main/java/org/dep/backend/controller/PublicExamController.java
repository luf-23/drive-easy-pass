package org.dep.backend.controller;

import org.dep.backend.dto.ExamScheduleCard;
import org.dep.backend.dto.ExamVenueDetail;
import org.dep.backend.dto.ExamVenueSummary;
import org.dep.backend.service.ExamRegistrationService;
import org.dep.backend.service.ExamVenueService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/public")
public class PublicExamController {
    private final ExamRegistrationService examRegistrationService;
    private final ExamVenueService examVenueService;

    public PublicExamController(
            ExamRegistrationService examRegistrationService,
            ExamVenueService examVenueService
    ) {
        this.examRegistrationService = examRegistrationService;
        this.examVenueService = examVenueService;
    }

    @GetMapping("/exam-schedules")
    public List<ExamScheduleCard> listSchedules(@RequestParam(required = false) String examType) {
        return examRegistrationService.listPublicSchedules(examType);
    }

    @GetMapping("/exam-venues")
    public List<ExamVenueSummary> listVenues() {
        return examVenueService.listPublic();
    }

    @GetMapping("/exam-venues/{id}")
    public ExamVenueDetail venueDetail(@PathVariable Long id) {
        try {
            return examVenueService.getDetail(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
