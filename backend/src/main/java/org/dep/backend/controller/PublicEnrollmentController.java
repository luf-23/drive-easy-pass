package org.dep.backend.controller;

import org.dep.backend.dto.CoursePackageDto;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.PublicEnrollmentIntentRequest;
import org.dep.backend.service.CoursePackageService;
import org.dep.backend.service.EnrollmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public")
public class PublicEnrollmentController {
    private final CoursePackageService coursePackageService;
    private final EnrollmentService enrollmentService;

    public PublicEnrollmentController(CoursePackageService coursePackageService, EnrollmentService enrollmentService) {
        this.coursePackageService = coursePackageService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/course-packages")
    public List<CoursePackageDto> coursePackages() {
        return coursePackageService.listPublic();
    }

    @PostMapping("/enrollment-intents")
    public EnrollmentLeadDto createEnrollmentIntent(@RequestBody PublicEnrollmentIntentRequest request) {
        return enrollmentService.createPublic(request);
    }
}
