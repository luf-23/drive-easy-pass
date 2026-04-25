package org.dep.backend.dto;

public record AuthRequest(
        String username,
        String password,
        String nickname
) {
}
