package org.dep.backend.service;

import org.dep.backend.dto.AuthRequest;
import org.dep.backend.dto.AuthResponse;
import org.dep.backend.dto.UserProfile;
import org.dep.backend.model.AppUser;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.security.JwtService;
import org.dep.backend.security.PasswordHasher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    private final RowMapper<AppUser> userMapper = (rs, rowNum) -> new AppUser(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("nickname"),
            rs.getTimestamp("create_time").toLocalDateTime()
    );

    public AuthService(JdbcTemplate jdbcTemplate, PasswordHasher passwordHasher, JwtService jwtService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());
        String nickname = normalizeNickname(request.nickname(), username);

        try {
            jdbcTemplate.update("""
                    INSERT INTO users (username, password_hash, nickname)
                    VALUES (?, ?, ?)
                    """, username, passwordHasher.hash(password), nickname);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = findByUsername(username);
        assignDefaultRole(user.id(), "student");
        return toAuthResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());
        AppUser user = findByUsername(username);

        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return toAuthResponse(user);
    }

    public UserProfile profile(CurrentUser currentUser) {
        AppUser user = findById(currentUser.id());
        return toUserProfile(user);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        String token = jwtService.createToken(user.id(), user.username());
        return new AuthResponse(token, toUserProfile(user));
    }

    private UserProfile toUserProfile(AppUser user) {
        List<String> roles = findRoleCodes(user.id());
        if (roles.isEmpty()) {
            roles = List.of(user.username().equals("admin") ? "admin" : "student");
        }
        List<String> permissions = roles.contains("admin") ? List.of("*:*:*") : List.of("drive:study");
        return new UserProfile(user.id(), user.username(), user.nickname(), roles, permissions);
    }

    private List<String> findRoleCodes(Long userId) {
        return jdbcTemplate.queryForList("""
                SELECT r.code
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = ? AND r.enabled = 1
                ORDER BY r.id
                """, String.class, userId);
    }

    private void assignDefaultRole(Long userId, String roleCode) {
        jdbcTemplate.update("""
                INSERT IGNORE INTO user_roles (user_id, role_id)
                SELECT ?, id FROM roles WHERE code = ?
                """, userId, roleCode);
    }

    private AppUser findByUsername(String username) {
        List<AppUser> users = jdbcTemplate.query("""
                SELECT id, username, password_hash, nickname, create_time
                FROM users
                WHERE username = ?
                """, userMapper, username);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return users.getFirst();
    }

    private AppUser findById(Long id) {
        List<AppUser> users = jdbcTemplate.query("""
                SELECT id, username, password_hash, nickname, create_time
                FROM users
                WHERE id = ?
                """, userMapper, id);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return users.getFirst();
    }

    private String normalizeUsername(String username) {
        String value = username == null ? "" : username.trim();
        if (!value.matches("[A-Za-z0-9_]{3,20}")) {
            throw new IllegalArgumentException("Username must be 3-20 letters, numbers, or underscores");
        }
        return value;
    }

    private String normalizePassword(String password) {
        String value = password == null ? "" : password;
        if (value.length() < 6 || value.length() > 32) {
            throw new IllegalArgumentException("Password length must be 6-32 characters");
        }
        return value;
    }

    private String normalizeNickname(String nickname, String username) {
        String value = nickname == null ? "" : nickname.trim();
        if (value.isBlank()) {
            return username;
        }
        if (value.length() > 20) {
            throw new IllegalArgumentException("Nickname must be at most 20 characters");
        }
        return value;
    }
}
