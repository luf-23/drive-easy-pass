package org.dep.backend.controller;

import java.util.List;

import org.dep.backend.dto.EnrollmentDashboardDto;
import org.dep.backend.dto.EnrollmentFollowUpDto;
import org.dep.backend.dto.EnrollmentFollowUpRequest;
import org.dep.backend.dto.EnrollmentLeadDto;
import org.dep.backend.dto.EnrollmentLeadRequest;
import org.dep.backend.dto.PageResult;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.AdminService;
import org.dep.backend.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final AdminService adminService;

    public EnrollmentController(EnrollmentService enrollmentService, AdminService adminService) {
        this.enrollmentService = enrollmentService;
        this.adminService = adminService;
    }

    @GetMapping("/leads")
    public PageResult<EnrollmentLeadDto> leads(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String source,
                                                @RequestParam(required = false) Long ownerUserId,
                                                @RequestParam(required = false) String startDate,
                                                @RequestParam(required = false) String endDate,
                                                @RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer pageSize,
                                                HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.queryLeads(keyword, status, source, ownerUserId, startDate, endDate, page, pageSize);
    }

    @GetMapping("/dashboard")
    public EnrollmentDashboardDto dashboard(HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.dashboard();
    }

    @GetMapping("/students")
    public PageResult<EnrollmentLeadDto> students(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.listSignedStudents(keyword, page, pageSize);
    }

    @GetMapping("/leads/{id}")
    public EnrollmentLeadDto lead(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.findLead(id);
    }

    @PostMapping("/leads")
    public EnrollmentLeadDto createLead(@RequestBody EnrollmentLeadRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.createLead(body);
    }

    @PutMapping("/leads/{id}")
    public EnrollmentLeadDto updateLead(@PathVariable Long id,
                                        @RequestBody EnrollmentLeadRequest body,
                                        HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.updateLead(id, body);
    }

    @PutMapping("/leads/{id}/owner/{ownerUserId}")
    public EnrollmentLeadDto assignOwner(@PathVariable Long id,
                                         @PathVariable Long ownerUserId,
                                         HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.assignOwner(id, ownerUserId);
    }

    @GetMapping("/leads/{id}/follow-ups")
    public List<EnrollmentFollowUpDto> followUps(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return enrollmentService.listFollowUps(id);
    }

    @PostMapping("/leads/{id}/follow-ups")
    public EnrollmentFollowUpDto addFollowUp(@PathVariable Long id,
                                             @RequestBody EnrollmentFollowUpRequest body,
                                             HttpServletRequest request) {
        CurrentUser currentUser = requireAdmin(request);
        return enrollmentService.addFollowUp(id, body, currentUser.id());
    }

    private CurrentUser requireAdmin(HttpServletRequest request) {
        CurrentUser currentUser = (CurrentUser) request.getAttribute("currentUser");
        if (currentUser == null || !adminService.hasAnyRole(currentUser.id(), List.of("admin", "sales", "market", "coach"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Enrollment staff role required");
        }
        return currentUser;
    }
}
