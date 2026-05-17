package org.dep.backend.service;

import java.util.List;

import org.dep.backend.dto.AppRouteDto;
import org.dep.backend.dto.AppRouteRequest;
import org.dep.backend.dto.RoleDto;
import org.dep.backend.dto.RoleRequest;
import org.dep.backend.mapper.AdminMapper;
import org.dep.backend.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final AdminMapper adminMapper;

    public AdminService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public boolean isAdmin(Long userId, String username) {
        if ("admin".equals(username)) {
            return true;
        }
        Integer count = adminMapper.countAdminRole(userId);
        return count != null && count > 0;
    }

    public boolean hasAnyRole(Long userId, List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return false;
        }
        Integer count = adminMapper.countAnyRole(userId, roleCodes);
        return count != null && count > 0;
    }

    public List<AppRouteDto> routes() {
        return adminMapper.listRoutes();
    }

    public List<AppRouteDto> routesForUser(CurrentUser currentUser) {
        String role = roleOf(currentUser.id(), currentUser.username());
        return adminMapper.listRoutesByRole(role);
    }

    public AppRouteDto createRoute(AppRouteRequest request) {
        validateRoute(request);
        adminMapper.insertRoute(
            normalize(request.path()),
            normalize(request.name()),
            normalize(request.title()),
            request.parentId(),
            normalizeNullable(request.redirect()),
            normalizeNullable(request.component()),
            normalizeNullable(request.icon()),
            request.rankNo() == null ? 0 : request.rankNo(),
            request.enabled() == null || request.enabled()
        );
        Long id = adminMapper.lastInsertId();
        return findRoute(id);
    }

    public AppRouteDto updateRoute(Long id, AppRouteRequest request) {
        validateRoute(request);
        adminMapper.updateRoute(
            id,
            normalize(request.path()),
            normalize(request.name()),
            normalize(request.title()),
            request.parentId(),
            normalizeNullable(request.redirect()),
            normalizeNullable(request.component()),
            normalizeNullable(request.icon()),
            request.rankNo() == null ? 0 : request.rankNo(),
            request.enabled() == null || request.enabled()
        );
        return findRoute(id);
    }

    @Transactional
    public void deleteRoute(Long id) {
        adminMapper.deleteRoleRoutesByRouteId(id);
        adminMapper.deleteRouteById(id);
    }

    public List<RoleDto> roles() {
        return List.of(
            new RoleDto(1L, "student", "学员", "驾考业务（练习/模考/错题）与报考业务（报名等门户页）", true, routeIdsByRole("student")),
            new RoleDto(2L, "admin", "管理员", "中台管理（招生、教学、报考服务、系统配置等）", true, routeIdsByRole("admin")),
            new RoleDto(3L, "coach", "教练", "科目二/科目三训练记录、学员进度与考前跟进（预留角色）", true, routeIdsByRole("coach"))
        );
    }

    public RoleDto createRole(RoleRequest request) {
        throw new IllegalArgumentException("Roles are fixed: student, coach and admin");
    }

    @Transactional
    public RoleDto updateRole(Long id, RoleRequest request) {
        String role = roleCodeById(id);
        replaceRoleRoutes(role, request.routeIds());
        return roles().stream()
            .filter(item -> item.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }

    public void deleteRole(Long id) {
        throw new IllegalArgumentException("Roles are fixed: student, coach and admin");
    }

    private String roleOf(Long userId, String username) {
        if ("admin".equals(username)) {
            return "admin";
        }
        String role = adminMapper.findUserRole(userId);
        if ("admin".equals(role) || "student".equals(role) || "coach".equals(role)) {
            return role;
        }
        return "student";
    }

    private String roleCodeById(Long id) {
        if (Long.valueOf(1L).equals(id)) return "student";
        if (Long.valueOf(2L).equals(id)) return "admin";
        if (Long.valueOf(3L).equals(id)) return "coach";
        throw new IllegalArgumentException("Role not found");
    }

    private AppRouteDto findRoute(Long id) {
        return adminMapper.findRouteById(id);
    }

    private List<Long> routeIdsByRole(String role) {
        return adminMapper.listRouteIdsByRole(role);
    }

    private void replaceRoleRoutes(String role, List<Long> routeIds) {
        adminMapper.deleteRoleRoutesByRole(role);
        if (routeIds == null) return;
        for (Long routeId : routeIds) {
            adminMapper.insertRoleRoute(role, routeId);
        }
    }

    private void validateRoute(AppRouteRequest request) {
        if (request == null || normalize(request.path()).isBlank() || normalize(request.name()).isBlank() || normalize(request.title()).isBlank()) {
            throw new IllegalArgumentException("Route path, name and title are required");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null ? "" : value.trim();
    }
}
