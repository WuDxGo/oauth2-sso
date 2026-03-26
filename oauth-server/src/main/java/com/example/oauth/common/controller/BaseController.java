package com.example.oauth.common.controller;

import com.example.oauth.common.result.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 基础控制器
 * 提供通用的响应处理方法
 */
@Slf4j
public abstract class BaseController {

    /**
     * 处理成功响应（带数据）
     */
    protected <T> Result<T> handleSuccess(Supplier<T> supplier, String successMessage) {
        try {
            T data = supplier.get();
            log.info("{}: {}", successMessage, data);
            return Result.success(successMessage, data);
        } catch (Exception e) {
            log.error("操作失败", e);
            return Result.failure("操作失败：" + e.getMessage());
        }
    }

    /**
     * 处理成功响应（无数据）
     */
    protected Result<Void> handleSuccess(String successMessage) {
        log.info("{}", successMessage);
        return Result.success(successMessage, null);
    }

    /**
     * 处理 null 值判断
     */
    protected <T> Result<T> handleNotNull(T data, String errorMessage) {
        if (data == null) {
            return Result.failure(errorMessage);
        }
        return Result.success(data);
    }

    /**
     * 获取当前登录用户名
     */
    protected String getCurrentUsername() {
        // 从 Spring Security 上下文中获取用户名
        org.springframework.security.core.context.SecurityContext context =
            org.springframework.security.core.context.SecurityContextHolder.getContext();
        if (context.getAuthentication() != null) {
            Object principal = context.getAuthentication().getPrincipal();
            
            // 如果是 UserDetails 类型
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }
            
            // 如果是 JWT Token 类型
            if (principal instanceof org.springframework.security.oauth2.jwt.Jwt) {
                org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) principal;
                return jwt.getSubject();  // sub 字段就是用户名
            }
            
            // 其他情况直接返回字符串
            return principal.toString();
        }
        return null;
    }
}
