package com.example.oauth.common.controller;

import com.example.oauth.common.result.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 基础控制器抽象类
 * 提供所有控制器都可使用的通用方法和工具函数
 * 子类控制器通过继承此类获得统一的响应处理和用户信息获取能力
 * 使用抽象类设计,避免直接实例化
 */
@Slf4j
public abstract class BaseController {

    /**
     * 处理业务逻辑并返回成功响应的通用方法
     * 封装try-catch逻辑,统一处理异常和日志记录
     * 适用于需要返回数据的成功场景
     *
     * @param supplier 业务逻辑提供者,执行具体的业务操作并返回数据
     * @param successMessage 成功时的日志描述信息
     * @param <T> 返回数据的泛型类型
     * @return 成功时返回包含数据的Result对象,失败时返回错误信息
     */
    protected <T> Result<T> handleSuccess(Supplier<T> supplier, String successMessage) {
        try {
            // 执行业务逻辑获取数据
            T data = supplier.get();
            // 记录成功日志,包含返回的数据
            log.info("{}: {}", successMessage, data);
            // 返回成功响应,包含业务数据
            return Result.success(successMessage, data);
        } catch (Exception e) {
            // 捕获业务异常,记录错误日志和堆栈信息
            log.error("操作失败", e);
            // 返回失败响应,包含异常信息
            return Result.failure("操作失败：" + e.getMessage());
        }
    }

    /**
     * 处理无返回数据的成功响应方法
     * 适用于仅需返回成功状态的场景,如删除、更新等操作
     *
     * @param successMessage 成功时的日志描述信息
     * @return 成功响应对象,data字段为null
     */
    protected Result<Void> handleSuccess(String successMessage) {
        // 记录成功日志
        log.info("{}", successMessage);
        // 返回成功响应,不携带业务数据
        return Result.success(successMessage, null);
    }

    /**
     * 处理数据非空校验的通用方法
     * 用于查询场景,当数据不存在时返回错误响应
     * 避免在控制器中重复编写null判断逻辑
     *
     * @param data 需要校验的数据对象
     * @param errorMessage 数据为null时的错误提示信息
     * @param <T> 数据对象的泛型类型
     * @return 数据非空时返回成功响应,为null时返回错误响应
     */
    protected <T> Result<T> handleNotNull(T data, String errorMessage) {
        // 判断数据是否为null
        if (data == null) {
            // 数据不存在,返回错误响应
            return Result.failure(errorMessage);
        }
        // 数据存在,返回成功响应
        return Result.success(data);
    }

    /**
     * 获取当前登录用户的用户名
     * 从Spring Security的SecurityContextHolder中提取认证信息
     * 支持多种认证类型:表单登录、JWT Token等
     *
     * @return 当前认证用户的用户名,如果未认证则返回null
     */
    protected String getCurrentUsername() {
        // 从Spring Security上下文中获取当前线程的认证信息
        // SecurityContextHolder是线程安全的,存储每个请求的认证状态
        org.springframework.security.core.context.SecurityContext context =
            org.springframework.security.core.context.SecurityContextHolder.getContext();
        // 判断当前请求是否已通过认证
        if (context.getAuthentication() != null) {
            // 获取认证主体对象(可以是不同类型的用户信息)
            Object principal = context.getAuthentication().getPrincipal();

            // 情况1:如果是UserDetails类型(通常来自表单登录或密码模式)
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 强制转换为UserDetails并获取用户名
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }

            // 情况2:如果是Jwt类型(来自JWT Token认证)
            if (principal instanceof org.springframework.security.oauth2.jwt.Jwt) {
                // 强制转换为Jwt对象
                org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) principal;
                // 从JWT的subject字段中提取用户名
                // subject是JWT标准声明,通常存储用户唯一标识
                return jwt.getSubject();
            }

            // 情况3:其他类型的主体(如字符串、自定义对象等)
            // 直接调用toString()方法转换为字符串
            return principal.toString();
        }
        // 当前请求未认证,返回null
        return null;
    }
}
