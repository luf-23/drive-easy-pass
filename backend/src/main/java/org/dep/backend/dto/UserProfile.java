package org.dep.backend.dto;

import java.util.List;

public record UserProfile(
        Long id,
        String username,
        String nickname,
        List<String> roles,
        List<String> permissions
) {
}
