package org.dep.backend.service;

import java.util.List;

import org.dep.backend.dto.AppRouteDto;
import org.dep.backend.dto.AppRouteRequest;
import org.dep.backend.dto.RoleDto;
import org.dep.backend.dto.RoleRequest;
import org.dep.backend.mapper.AdminMapper;
import org.dep.backend.mapper.projection.RoleBaseRecord;
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

    public AppRouteDto createRoute(AppRouteRequest request) {
        validateRoute(request);
        adminMapper.insertRoute(
            normalize(request.path()),
            normalize(request.name()),
            normalize(request.title()),
            request.parentId(),
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
        return adminMapper.listRoles().stream()
            .map(role -> new RoleDto(
                role.id(),
                role.code(),
                role.name(),
                role.description(),
                role.enabled(),
                routeIdsByRole(role.id())
            ))
            .toList();
    }

    @Transactional
    public RoleDto createRole(RoleRequest request) {
        validateRole(request);
        adminMapper.insertRole(
                normalize(request.code()),
                normalize(request.name()),
                normalizeNullable(request.description()),
                request.enabled() == null || request.enabled()
        );
        Long id = adminMapper.lastInsertId();
        replaceRoleRoutes(id, request.routeIds());
        return findRole(id);
    }

    @Transactional
    public RoleDto updateRole(Long id, RoleRequest request) {
        validateRole(request);
        adminMapper.updateRole(
                id,
                normalize(request.code()),
                normalize(request.name()),
                normalizeNullable(request.description()),
                request.enabled() == null || request.enabled()
        );
        replaceRoleRoutes(id, request.routeIds());
        return findRole(id);
    }

    @Transactional
    public void deleteRole(Long id) {
        adminMapper.deleteRoleRoutesByRoleId(id);
        adminMapper.deleteUserRolesByRoleId(id);
        adminMapper.deleteRoleById(id);
    }

    private AppRouteDto findRoute(Long id) {
        return adminMapper.findRouteById(id);
    }

    private RoleDto findRole(Long id) {
        RoleBaseRecord role = adminMapper.findRoleById(id);
        if (role == null) {
            throw new IllegalArgumentException("Role not found");
        }
        return new RoleDto(
                role.id(),
                role.code(),
                role.name(),
                role.description(),
                role.enabled(),
                routeIdsByRole(role.id())
        );
    }

    private List<Long> routeIdsByRole(Long roleId) {
        return adminMapper.listRouteIdsByRoleId(roleId);
    }

    private void replaceRoleRoutes(Long roleId, List<Long> routeIds) {
        adminMapper.deleteRoleRoutesByRoleId(roleId);
        if (routeIds == null) return;
        for (Long routeId : routeIds) {
            adminMapper.insertRoleRoute(roleId, routeId);
        }
    }

    private void validateRoute(AppRouteRequest request) {
        if (request == null || normalize(request.path()).isBlank() || normalize(request.name()).isBlank() || normalize(request.title()).isBlank()) {
            throw new IllegalArgumentException("Route path, name and title are required");
        }
    }

    private void validateRole(RoleRequest request) {
        if (request == null || normalize(request.code()).isBlank() || normalize(request.name()).isBlank()) {
            throw new IllegalArgumentException("Role code and name are required");
        }
        if (!normalize(request.code()).matches("[A-Za-z0-9_:-]{2,50}")) {
            throw new IllegalArgumentException("Role code format is invalid");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        return value == null ? "" : value.trim();
    }

    private int safeRank(Integer rankNo) {
        return rankNo == null ? 0 : rankNo;
    }
}
