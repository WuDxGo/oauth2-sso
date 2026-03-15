package com.example.oauth.common.controller; // 定义包路径，用于组织和管理 Java 基础控制器类

import com.example.oauth.common.result.Result; // 导入统一返回结果类
import org.springframework.security.core.context.SecurityContextHolder; // 导入 Spring Security 上下文持有者

import java.util.function.Supplier; // 导入 Supplier 函数式接口

/**
 * 基础控制器类
 * 提供通用的 Controller 方法，减少代码重复
 */
public abstract class BaseController { // 定义基础控制器抽象类

    /**
     * 处理业务逻辑并返回成功响应
     * @param supplier 业务逻辑供应商
     * @param <T> 返回数据类型
     * @return Result<T> 统一的成功响应结果
     */
    protected <T> Result<T> handleSuccess(Supplier<T> supplier) { // 处理成功响应的通用方法
        return Result.success(supplier.get()); // 调用供应商获取数据并返回成功响应
    }

    /**
     * 处理业务逻辑并返回成功响应（带自定义消息）
     * @param supplier 业务逻辑供应商
     * @param message 成功消息
     * @param <T> 返回数据类型
     * @return Result<T> 统一的成功响应结果
     */
    protected <T> Result<T> handleSuccess(Supplier<T> supplier, String message) { // 处理带消息的成功响应方法
        return Result.success(message, supplier.get()); // 调用供应商获取数据并返回带消息的成功响应
    }

    /**
     * 处理 null 值判断并返回响应
     * @param entity 待检查的实体对象
     * @param errorMessage 错误消息
     * @param <T> 实体类型
     * @return Result<T> 如果实体为 null 返回错误响应，否则返回成功响应
     */
    protected <T> Result<T> handleNotNull(T entity, String errorMessage) { // 处理非空判断的通用方法
        if (entity == null) { // 判断实体是否为 null
            return Result.error(404, errorMessage); // 如果为 null，返回 404 错误响应
        }
        return Result.success(entity); // 如果不为 null，返回成功响应
    }

    /**
     * 获取当前登录用户名
     * @return String 当前登录用户名
     */
    protected String getCurrentUsername() { // 获取当前用户名的方法
        return SecurityContextHolder.getContext().getAuthentication().getName(); // 从 Spring Security 上下文中获取当前登录用户名
    }
}
