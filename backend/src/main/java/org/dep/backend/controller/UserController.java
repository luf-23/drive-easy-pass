package org.dep.backend.controller;

import org.dep.backend.dto.PageResult;
import org.dep.backend.dto.UserDto;
import org.dep.backend.model.AppUser;
import org.dep.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResult<AppUser> listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.listUsers(username, email, role, page, size);
    }

    @PostMapping
    public Map<String, Object> createUser(@RequestBody UserDto.CreateRequest request) {
        AppUser user = new AppUser(
                null,
                request.username(),
                request.password(),
                request.nickname(),
                request.role(),
                request.email(),
                1,
                null
        );
        userService.createUser(user);
        return Map.of("success", true);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody UserDto.UpdateRequest request) {
        AppUser user = new AppUser(
                id,
                request.username(),
                null,
                request.nickname(),
                request.role(),
                request.email(),
                null,
                null
        );
        userService.updateUser(id, user);
        return Map.of("success", true);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Map.of("success", true);
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody UserDto.StatusRequest request) {
        userService.updateStatus(id, request.status());
        return Map.of("success", true);
    }

    @PutMapping("/{id}/password")
    public Map<String, Object> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Map.of("success", true);
    }
}