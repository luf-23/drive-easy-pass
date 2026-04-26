package org.dep.backend.security;

public record CurrentUser(
        Long id,
        String username
) {
}
