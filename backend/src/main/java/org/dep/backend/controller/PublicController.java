package org.dep.backend.controller;

import java.util.List;

import org.dep.backend.dto.CoursePackageDto;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.ExamSiteDto;
import org.dep.backend.dto.PublicEnrollmentIntentRequest;
import org.dep.backend.service.EnrollmentService;
import org.dep.backend.service.PublicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicController {
    private final PublicService publicService;
    private final EnrollmentService enrollmentService;

    public PublicController(PublicService publicService, EnrollmentService enrollmentService) {
        this.publicService = publicService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/course-packages")
    public List<CoursePackageDto> coursePackages() {
        return publicService.listCoursePackages();
    }

    @GetMapping("/exam-sites")
    public List<ExamSiteDto> examSites() {
        return publicService.listExamSites();
    }

    @PostMapping("/enrollment-intents")
    public EnrollmentLeadDto submitIntent(@RequestBody PublicEnrollmentIntentRequest body) {
        return enrollmentService.createLeadFromPublicIntent(body);
    }
}
