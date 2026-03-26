package com.example.oauth.common.constant; // 定义包路径，用于组织和管理 Java 常量类

/**
 * 系统常量类
 * 定义了系统中使用的各种公共常量值
 */
public class CommonConstants { // 定义公共常量类

    /**
     * JWT Token 中存储用户 ID 的键名
     */
    public static final String USER_ID = "user_id"; // 用户 ID 常量，用于从 JWT 中提取用户 ID

    /**
     * JWT Token 中存储用户名的键名
     */
    public static final String USERNAME = "username"; // 用户名常量，用于从 JWT 中提取用户名

    /**
     * JWT Token 中存储权限信息的键名
     */
    public static final String AUTHORITIES = "authorities"; // 权限信息常量，用于从 JWT 中提取用户权限

    /**
     * HTTP Authorization 头中 Bearer Token 的前缀
     */
    public static final String BEARER_PREFIX = "Bearer "; // Bearer Token 前缀常量，用于解析 Authorization 头

    /**
     * HTTP 请求头名称 - Authorization
     * 用于传递认证 Token
     */
    public static final String AUTHORIZATION_HEADER = "Authorization"; // Authorization 请求头常量，用于传递 JWT Token

    /**
     * HTTP 请求头名称 - 用户 ID
     * 用于传递当前登录用户的 ID
     */
    public static final String USER_ID_HEADER = "X-User-Id"; // 用户 ID 请求头常量，用于在微服务间传递用户 ID

    /**
     * HTTP 请求头名称 - 用户名
     * 用于传递当前登录用户的用户名
     */
    public static final String USERNAME_HEADER = "X-Username"; // 用户名请求头常量，用于在微服务间传递用户名

    /**
     * 成功状态码
     * 表示操作成功完成
     */
    public static final int SUCCESS_CODE = 200; // 成功状态码常量，表示请求成功处理

    /**
     * 失败状态码
     * 表示服务器内部错误
     */
    public static final int ERROR_CODE = 500; // 错误状态码常量，表示服务器内部错误

    /**
     * 未授权状态码
     * 表示请求需要认证或认证失败
     */
    public static final int UNAUTHORIZED_CODE = 401; // 未授权状态码常量，表示认证失败或未认证

    /**
     * 禁止访问状态码
     * 表示已认证但没有权限访问资源
     */
    public static final int FORBIDDEN_CODE = 403; // 禁止访问状态码常量，表示权限不足

    /**
     * 未找到状态码
     * 表示请求的资源不存在
     */
    public static final int NOT_FOUND_CODE = 404; // 未找到状态码常量，表示资源不存在
}
