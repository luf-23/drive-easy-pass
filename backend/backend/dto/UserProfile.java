package org.dep.backend.dto;

public record UserProfile(
        Long id,
        String username,
        String nickname
) {
}
