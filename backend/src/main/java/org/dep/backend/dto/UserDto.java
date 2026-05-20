package org.dep.backend.dto;

public class UserDto {
    
    public record CreateRequest(
            String username,
            String nickname,
            String role,
            String email,
            String password
    ) {}

    public record UpdateRequest(
            String username,
            String nickname,
            String role,
            String email
    ) {}

    public record StatusRequest(
            Object status
    ) {}
}
