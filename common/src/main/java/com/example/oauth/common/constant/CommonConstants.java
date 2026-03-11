package com.example.oauth.common.constant;

/**
 * 系统常量类
 */
public class CommonConstants {

    /**
     * 用户 ID
     */
    public static final String USER_ID = "user_id";

    /**
     * 用户名
     */
    public static final String USERNAME = "username";

    /**
     * 权限信息
     */
    public static final String AUTHORITIES = "authorities";

    /**
     * JWT Token 前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * HTTP 头名称 - Authorization
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * HTTP 头名称 - 用户 ID
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * HTTP 头名称 - 用户名
     */
    public static final String USERNAME_HEADER = "X-Username";

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 未找到状态码
     */
    public static final int NOT_FOUND_CODE = 404;
}
