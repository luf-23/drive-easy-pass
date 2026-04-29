package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.AppRouteDto;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {
    private final AdminService adminService;

    public RouteController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<AppRouteDto> currentUserRoutes(HttpServletRequest request) {
        CurrentUser currentUser = (CurrentUser) request.getAttribute("currentUser");
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first");
        }
        return adminService.routesForUser(currentUser);
    }
}
