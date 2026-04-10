package com.example.oauth.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    /** JWT 访问令牌 */
    private String accessToken;

    /** Token 类型，默认 Bearer */
    private String tokenType;

    /** Token 有效期（秒） */
    private long expiresIn;

    /** 授权范围 */
    private String scope;

    public LoginResponse(String accessToken, long expiresIn, String scope) {
        this(accessToken, "Bearer", expiresIn, scope);
    }
}
