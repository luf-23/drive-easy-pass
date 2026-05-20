package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.ExamVenueDetail;
import org.dep.backend.dto.ExamVenueRoutesUpdateRequest;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.AdminService;
import org.dep.backend.service.ExamVenueService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/exam-venues")
public class ExamVenueAdminController {
    private final ExamVenueService examVenueService;
    private final AdminService adminService;

    public ExamVenueAdminController(ExamVenueService examVenueService, AdminService adminService) {
        this.examVenueService = examVenueService;
        this.adminService = adminService;
    }

    @PutMapping("/{id}/routes")
    public ExamVenueDetail updateRoutes(
            @PathVariable Long id,
            @RequestBody ExamVenueRoutesUpdateRequest body,
            HttpServletRequest request
    ) {
        requireStaff(request);
        try {
            return examVenueService.saveRoutes(id, body);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private void requireStaff(HttpServletRequest request) {
        CurrentUser currentUser = (CurrentUser) request.getAttribute("currentUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }
        boolean allowed = adminService.isAdmin(currentUser.id(), currentUser.username())
                || adminService.hasAnyRole(currentUser.id(), List.of("coach"));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin or coach role required");
        }
    }
}
