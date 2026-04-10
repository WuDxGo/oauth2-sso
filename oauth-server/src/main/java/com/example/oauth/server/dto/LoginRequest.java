package com.example.oauth.server.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {
    /** 用户登录账号 */
    private String username;

    /** 用户登录密码 */
    private String password;
}
