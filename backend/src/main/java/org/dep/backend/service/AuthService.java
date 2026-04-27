package org.dep.backend.service;

import org.dep.backend.dto.AuthRequest;
import org.dep.backend.dto.AuthResponse;
import org.dep.backend.dto.UserProfile;
import org.dep.backend.mapper.AuthMapper;
import org.dep.backend.model.AppUser;
import org.dep.backend.security.CurrentUser;
import org.dep.backend.security.JwtService;
import org.dep.backend.security.PasswordHasher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    public AuthService(AuthMapper authMapper, PasswordHasher passwordHasher, JwtService jwtService) {
        this.authMapper = authMapper;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRequest request) {
        String username = normalizeUsername(request.username());
        String password = normalizePassword(request.password());
        String nickname = normalizeNickname(request.nickname(), username);

        try {
            authMapper.insertUser(username, passwordHasher.hash(password), nickname);
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
        return authMapper.findRoleCodes(userId);
    }

    private void assignDefaultRole(Long userId, String roleCode) {
        authMapper.assignRoleByCode(userId, roleCode);
    }

    private AppUser findByUsername(String username) {
        AppUser user = authMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    private AppUser findById(Long id) {
        AppUser user = authMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
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
