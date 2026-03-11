package com.example.oauth.user.controller;

import com.example.oauth.common.result.Result;
import com.example.oauth.user.entity.User;
import com.example.oauth.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取所有用户
     */
    @GetMapping
    @PreAuthorize("hasAuthority('read')")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return Result.success(users);
    }

    /**
     * 根据 ID 获取用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('read')")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('read')")
    public Result<User> getCurrentUser() {
        // 从 JWT Token 中获取用户信息
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('write')")
    public Result<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return Result.success("创建成功", created);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('write')")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updated = userService.update(user);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('write')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
