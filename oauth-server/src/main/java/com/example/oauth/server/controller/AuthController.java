package com.example.oauth.server.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * 简单登录控制器 - 支持前后端分离
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * 登录响应
     */
    @Data
    public static class LoginResponse {
        private String access_token;
        private String token_type = "Bearer";
        private long expires_in;
        private String scope;

        public LoginResponse(String accessToken, long expiresIn) {
            this.access_token = accessToken;
            this.expires_in = expiresIn;
            this.scope = "read write";
        }
    }

    /**
     * 简单登录接口
     */
    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        log.info("用户登录：{}", request.getUsername());

        // 1. 认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 生成 JWT
        Instant now = Instant.now();
        Instant expiry = now.plus(2, ChronoUnit.HOURS);

        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("http://localhost:8080")
            .issuedAt(now)
            .expiresAt(expiry)
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        log.info("用户 {} 登录成功，生成 JWT Token", request.getUsername());

        return new LoginResponse(token, 7200);
    }

    /**
     * OAuth2 兼容登录接口（密码模式）
     */
    @PostMapping("/oauth2/token")
    public LoginResponse oauth2Login(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam(defaultValue = "password") String grant_type,
                                      @RequestParam(defaultValue = "read write") String scope) {
        log.info("OAuth2 登录：{}", username);

        // 1. 认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. 生成 JWT
        Instant now = Instant.now();
        Instant expiry = now.plus(2, ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("http://localhost:8080")
            .issuedAt(now)
            .expiresAt(expiry)
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        log.info("用户 {} OAuth2 登录成功", username);

        return new LoginResponse(token, 7200);
    }
}
