package org.dep.backend.service;

import org.dep.backend.dto.AppRouteDto;
import org.dep.backend.dto.AppRouteRequest;
import org.dep.backend.dto.RoleDto;
import org.dep.backend.dto.RoleRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AppRouteDto> routeMapper = (rs, rowNum) -> new AppRouteDto(
            rs.getLong("id"),
            rs.getString("path"),
            rs.getString("name"),
            rs.getString("title"),
            rs.getObject("parent_id", Long.class),
            rs.getString("component"),
            rs.getString("icon"),
            rs.getInt("rank_no"),
            rs.getBoolean("enabled")
    );

    public AdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isAdmin(Long userId, String username) {
        if ("admin".equals(username)) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = ? AND r.code = 'admin' AND r.enabled = 1
                """, Integer.class, userId);
        return count != null && count > 0;
    }

    public List<AppRouteDto> routes() {
        return jdbcTemplate.query("""
                SELECT id, path, name, title, parent_id, component, icon, rank_no, enabled
                FROM app_routes
                ORDER BY rank_no, id
                """, routeMapper);
    }

    public AppRouteDto createRoute(AppRouteRequest request) {
        validateRoute(request);
        jdbcTemplate.update("""
                INSERT INTO app_routes (path, name, title, parent_id, component, icon, rank_no, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                normalize(request.path()),
                normalize(request.name()),
                normalize(request.title()),
                request.parentId(),
                normalizeNullable(request.component()),
                normalizeNullable(request.icon()),
                request.rankNo() == null ? 0 : request.rankNo(),
                request.enabled() == null || request.enabled()
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findRoute(id);
    }

    public AppRouteDto updateRoute(Long id, AppRouteRequest request) {
        validateRoute(request);
        jdbcTemplate.update("""
                UPDATE app_routes
                SET path = ?, name = ?, title = ?, parent_id = ?, component = ?, icon = ?, rank_no = ?, enabled = ?
                WHERE id = ?
                """,
                normalize(request.path()),
                normalize(request.name()),
                normalize(request.title()),
                request.parentId(),
                normalizeNullable(request.component()),
                normalizeNullable(request.icon()),
                request.rankNo() == null ? 0 : request.rankNo(),
                request.enabled() == null || request.enabled(),
                id
        );
        return findRoute(id);
    }

    @Transactional
    public void deleteRoute(Long id) {
        jdbcTemplate.update("DELETE FROM role_routes WHERE route_id = ?", id);
        jdbcTemplate.update("DELETE FROM app_routes WHERE id = ?", id);
    }

    public List<RoleDto> roles() {
        return jdbcTemplate.query("""
                SELECT id, code, name, description, enabled
                FROM roles
                ORDER BY id
                """, (rs, rowNum) -> new RoleDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("enabled"),
                routeIdsByRole(rs.getLong("id"))
        ));
    }

    @Transactional
    public RoleDto createRole(RoleRequest request) {
        validateRole(request);
        jdbcTemplate.update("""
                INSERT INTO roles (code, name, description, enabled)
                VALUES (?, ?, ?, ?)
                """,
                normalize(request.code()),
                normalize(request.name()),
                normalizeNullable(request.description()),
                request.enabled() == null || request.enabled()
        );
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        replaceRoleRoutes(id, request.routeIds());
        return findRole(id);
    }

    @Transactional
    public RoleDto updateRole(Long id, RoleRequest request) {
        validateRole(request);
        jdbcTemplate.update("""
                UPDATE roles
                SET code = ?, name = ?, description = ?, enabled = ?
                WHERE id = ?
                """,
                normalize(request.code()),
                normalize(request.name()),
                normalizeNullable(request.description()),
                request.enabled() == null || request.enabled(),
                id
        );
        replaceRoleRoutes(id, request.routeIds());
        return findRole(id);
    }

    @Transactional
    public void deleteRole(Long id) {
        jdbcTemplate.update("DELETE FROM role_routes WHERE role_id = ?", id);
        jdbcTemplate.update("DELETE FROM user_roles WHERE role_id = ?", id);
        jdbcTemplate.update("DELETE FROM roles WHERE id = ?", id);
    }

    private AppRouteDto findRoute(Long id) {
        return jdbcTemplate.queryForObject("""
                SELECT id, path, name, title, parent_id, component, icon, rank_no, enabled
                FROM app_routes
                WHERE id = ?
                """, routeMapper, id);
    }

    private RoleDto findRole(Long id) {
        return jdbcTemplate.queryForObject("""
                SELECT id, code, name, description, enabled
                FROM roles
                WHERE id = ?
                """, (rs, rowNum) -> new RoleDto(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("enabled"),
                routeIdsByRole(rs.getLong("id"))
        ), id);
    }

    private List<Long> routeIdsByRole(Long roleId) {
        return jdbcTemplate.queryForList("""
                SELECT route_id
                FROM role_routes
                WHERE role_id = ?
                ORDER BY route_id
                """, Long.class, roleId);
    }

    private void replaceRoleRoutes(Long roleId, List<Long> routeIds) {
        jdbcTemplate.update("DELETE FROM role_routes WHERE role_id = ?", roleId);
        if (routeIds == null) return;
        for (Long routeId : routeIds) {
            jdbcTemplate.update("INSERT IGNORE INTO role_routes (role_id, route_id) VALUES (?, ?)", roleId, routeId);
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
}
