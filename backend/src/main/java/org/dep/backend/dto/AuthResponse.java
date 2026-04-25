package org.dep.backend.dto;

public record AuthResponse(
        String token,
        UserProfile user
) {
}
