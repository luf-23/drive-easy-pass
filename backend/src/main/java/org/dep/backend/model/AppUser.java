package org.dep.backend.model;

import java.time.LocalDateTime;

public record AppUser(
        Long id,
        String username,
        String passwordHash,
        String nickname,
        LocalDateTime createTime
) {
}
