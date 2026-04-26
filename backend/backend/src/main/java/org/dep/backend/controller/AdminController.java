package org.dep.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dep.backend.dto.AppRouteDto;
import org.dep.backend.dto.AppRouteRequest;
import org.dep.backend.dto.RoleDto;
import org.dep.backend.dto.RoleRequest;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/routes")
    public List<AppRouteDto> routes(HttpServletRequest request) {
        requireAdmin(request);
        return adminService.routes();
    }

    @PostMapping("/routes")
    public AppRouteDto createRoute(@RequestBody AppRouteRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return adminService.createRoute(body);
    }

    @PutMapping("/routes/{id}")
    public AppRouteDto updateRoute(@PathVariable Long id, @RequestBody AppRouteRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return adminService.updateRoute(id, body);
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        adminService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public List<RoleDto> roles(HttpServletRequest request) {
        requireAdmin(request);
        return adminService.roles();
    }

    @PostMapping("/roles")
    public RoleDto createRole(@RequestBody RoleRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return adminService.createRole(body);
    }

    @PutMapping("/roles/{id}")
    public RoleDto updateRole(@PathVariable Long id, @RequestBody RoleRequest body, HttpServletRequest request) {
        requireAdmin(request);
        return adminService.updateRole(id, body);
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        adminService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(HttpServletRequest request) {
        CurrentUser currentUser = (CurrentUser) request.getAttribute("currentUser");
        if (currentUser == null || !adminService.isAdmin(currentUser.id(), currentUser.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}
