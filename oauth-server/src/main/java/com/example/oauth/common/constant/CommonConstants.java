package com.example.oauth.common.constant;

/**
 * 系统通用常量定义类
 * 集中管理系统中使用的所有常量,避免魔法数值和字符串散落在代码中
 * 使用final类防止被继承,所有字段都是public static final不可变
 * 按功能分类:JWT相关、HTTP请求头相关、HTTP状态码相关
 */
public class CommonConstants {

    /**
     * JWT Token载荷中存储用户ID的键名
     * 用于在JWT的claims中读取用户ID信息
     * 如:jwtPayload.get("user_id")返回用户ID值
     */
    public static final String USER_ID = "user_id";

    /**
     * JWT Token载荷中存储用户名的键名
     * 用于在JWT的claims中读取用户登录账号
     * 如:jwtPayload.get("username")返回用户名值
     */
    public static final String USERNAME = "username";

    /**
     * JWT Token载荷中存储权限信息的键名
     * 用于在JWT的claims中读取用户的权限列表
     * 如:jwtPayload.get("authorities")返回权限列表["read","write"]
     */
    public static final String AUTHORITIES = "authorities";

    /**
     * HTTP请求Authorization请求头中Bearer Token的前缀
     * OAuth2标准规范要求在Token前添加"Bearer "前缀
     * 如请求头:Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
     * 注意:Bearer后面有一个空格,解析时需要去除
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * HTTP认证请求头名称
     * 用于客户端携带JWT Token进行身份认证
     * 标准HTTP请求头,所有OAuth2/OIDC实现都使用此请求头
     * 如:request.getHeader("Authorization")获取Token值
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 自定义HTTP请求头名称-用户ID
     * 用于在微服务间传递当前登录用户的ID信息
     * 网关层从JWT中解析用户ID后,通过此请求头传递给下游服务
     * 命名遵循X-前缀的自定义请求头规范
     * 如:request.getHeader("X-User-Id")获取用户ID
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 自定义HTTP请求头名称-用户名
     * 用于在微服务间传递当前登录用户的用户名
     * 网关层从JWT中解析用户名后,通过此请求头传递给下游服务
     * 避免下游服务重复解析JWT,提高性能
     * 如:request.getHeader("X-Username")获取用户名
     */
    public static final String USERNAME_HEADER = "X-Username";

    /**
     * 成功状态码
     * HTTP状态码200,表示请求成功处理
     * 用于统一返回结果Result类的code字段
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 服务器内部错误状态码
     * HTTP状态码500,表示服务器遇到意外情况,无法完成请求
     * 通常用于未知异常、数据库错误、网络错误等场景
     */
    public static final int ERROR_CODE = 500;

    /**
     * 未授权状态码
     * HTTP状态码401,表示请求未通过身份认证
     * 常见场景:Token过期、Token无效、未携带Token
     * 客户端收到此状态码应引导用户重新登录
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     * HTTP状态码403,表示用户已通过认证但没有权限访问请求的资源
     * 常见场景:用户角色权限不足、访问了未授权的资源
     * 与401的区别:401是未认证,403是已认证但无权限
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 资源未找到状态码
     * HTTP状态码404,表示请求的资源不存在
     * 常见场景:查询的用户ID不存在、访问的API路径不存在
     */
    public static final int NOT_FOUND_CODE = 404;
}
