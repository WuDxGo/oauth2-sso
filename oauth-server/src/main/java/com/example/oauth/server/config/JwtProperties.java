package com.example.oauth.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT 签发者
     */
    private String issuer = "http://localhost:8080";
    
    /**
     * Access Token 有效期（秒）
     */
    private Long accessTokenTtl = 7200L;
    
    /**
     * Refresh Token 有效期（秒）
     */
    private Long refreshTokenTtl = 604800L;
}
