package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.AssignOwnerRequest;
import org.dep.backend.dto.CoursePackageDto;
import org.dep.backend.dto.CoursePackageRequest;
import org.dep.backend.dto.EnrollmentDashboardDto;
import org.dep.backend.dto.EnrollmentFollowUpDto;
import org.dep.backend.dto.EnrollmentFollowUpRequest;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.EnrollmentLeadRequest;
import org.dep.backend.dto.PageResult;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.AdminService;
import org.dep.backend.service.CoursePackageService;
import org.dep.backend.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminEnrollmentController {
    private final AdminService adminService;
    private final CoursePackageService coursePackageService;
    private final EnrollmentService enrollmentService;

    public AdminEnrollmentController(AdminService adminService,
                                     CoursePackageService coursePackageService,
                                     EnrollmentService enrollmentService) {
        this.adminService = adminService;
        this.coursePackageService = coursePackageService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/dashboard/summary")
    public EnrollmentDashboardDto dashboard(HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.dashboard();
    }

    @GetMapping("/enrollment-intents")
    public PageResult<EnrollmentLeadDto> enrollmentIntents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request
    ) {
        requireAdmin(request);
        return enrollmentService.list(keyword, status, source, startDate, endDate, page, pageSize);
    }

    @GetMapping("/students")
    public PageResult<EnrollmentLeadDto> students(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request
    ) {
        requireAdmin(request);
        return enrollmentService.listSigned(keyword, page, pageSize);
    }

    @PostMapping("/enrollment-intents")
    public EnrollmentLeadDto createLead(@RequestBody EnrollmentLeadRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.create(body);
    }

    @PutMapping("/enrollment-intents/{id}")
    public EnrollmentLeadDto updateLead(@PathVariable Long id, @RequestBody EnrollmentLeadRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.update(id, body);
    }

    @PatchMapping("/enrollment-intents/{id}/owner")
    public EnrollmentLeadDto assignOwner(@PathVariable Long id, @RequestBody AssignOwnerRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.assignOwner(id, body.ownerUserId());
    }

    @PatchMapping("/enrollment-intents/{id}/status")
    public EnrollmentLeadDto updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.updateStatus(id, body.get("status"));
    }

    @PostMapping("/enrollment-intents/{id}/convert-to-student")
    public EnrollmentLeadDto convertToStudent(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.convertToStudent(id);
    }

    @GetMapping("/enrollment-intents/{id}/follow-records")
    public List<EnrollmentFollowUpDto> followUps(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.followUps(id);
    }

    @PostMapping("/enrollment-intents/{id}/follow-records")
    public EnrollmentFollowUpDto createFollowUp(
            @PathVariable Long id,
            @RequestBody EnrollmentFollowUpRequest body,
            HttpServletRequest request
    ) {
        CurrentUser user = requireAdmin(request);
        return enrollmentService.addFollowUp(id, body, user.id());
    }

    @GetMapping("/course-packages")
    public List<CoursePackageDto> coursePackages(HttpServletRequest request) {
        requireAdmin(request);
        return coursePackageService.listAll();
    }

    @PostMapping("/course-packages")
    public CoursePackageDto createCoursePackage(@RequestBody CoursePackageRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return coursePackageService.create(body);
    }

    @PutMapping("/course-packages/{id}")
    public CoursePackageDto updateCoursePackage(@PathVariable Long id, @RequestBody CoursePackageRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return coursePackageService.update(id, body);
    }

    @PatchMapping("/course-packages/{id}/status")
    public CoursePackageDto updateCoursePackageStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body, HttpServletRequest request) {
        requireAdmin(request);
        return coursePackageService.updateStatus(id, body.get("enabled"));
    }

    @DeleteMapping("/course-packages/{id}")
    public ResponseEntity<Void> deleteCoursePackage(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        coursePackageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CurrentUser requireAdmin(HttpServletRequest request) {
        CurrentUser currentUser = (CurrentUser) request.getAttribute("currentUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }
        if (!adminService.isAdmin(currentUser.id(), currentUser.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return currentUser;
    }
}
