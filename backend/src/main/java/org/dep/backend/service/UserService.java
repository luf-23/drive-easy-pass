package org.dep.backend.service;

import org.dep.backend.dto.PageResult;
import org.dep.backend.mapper.UserMapper;
import org.dep.backend.model.AppUser;
import org.dep.backend.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;

    public UserService(UserMapper userMapper, PasswordHasher passwordHasher) {
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
    }

    public PageResult<AppUser> listUsers(String username, String email, String role, int page, int size) {
        int offset = (page - 1) * size;
        List<AppUser> users = userMapper.findUsersByCondition(username, email, role, offset, size);
        long total = userMapper.countUsersByCondition(username, email, role);
        return new PageResult<>(users, total);
    }

    @Transactional
    public void createUser(AppUser user) {
        if (userMapper.findByUsername(user.username()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        String encodedPassword = passwordHasher.hash(user.passwordHash() != null ? user.passwordHash() : "123456");
        AppUser newUser = new AppUser(
                null,
                user.username(),
                encodedPassword,
                user.nickname(),
                user.role() != null ? user.role() : "student",
                user.email(),
                user.status() != null ? user.status() : 1,
                null
        );
        userMapper.insert(newUser);
    }

    @Transactional
    public void updateUser(Long id, AppUser user) {
        AppUser existingUser = userMapper.findById(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        AppUser updated = new AppUser(
                id,
                user.username() != null ? user.username() : existingUser.username(),
                existingUser.passwordHash(),
                user.nickname() != null ? user.nickname() : existingUser.nickname(),
                user.role() != null ? user.role() : existingUser.role(),
                user.email() != null ? user.email() : existingUser.email(),
                existingUser.status(),
                existingUser.createTime()
        );
        userMapper.update(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    @Transactional
    public void resetPassword(Long id) {
        String encodedPassword = passwordHasher.hash("123456");
        userMapper.updatePassword(id, encodedPassword);
    }
}
