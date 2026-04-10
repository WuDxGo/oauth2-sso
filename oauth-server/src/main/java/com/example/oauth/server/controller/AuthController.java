package com.example.oauth.server.controller;

import com.example.oauth.server.config.JwtProperties;
import com.example.oauth.server.dto.LoginRequest;
import com.example.oauth.server.dto.LoginResponse;
import com.example.oauth.server.dto.UserInfoVO;
import com.example.oauth.server.service.UserService;
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
 * 认证控制器
 * 提供用户认证接口（登录、获取用户信息等）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    /**
     * 用户登录接口
     * 验证用户名密码，成功后返回 JWT Token
     *
     * @param request 登录请求体
     * @return 登录响应（包含 Token）
     */
    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Instant now = Instant.now();
        long ttl = jwtProperties.getAccessTokenTtl();
        Instant expiry = now.plus(ttl, ChronoUnit.SECONDS);

        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())
            .issuedAt(now)
            .expiresAt(expiry)
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponse(token, ttl, scope);
    }

    /**
     * 获取当前登录用户信息
     * 需要携带有效的 JWT Token
     *
     * @param authentication 当前认证对象
     * @return 用户详细信息
     */
    @GetMapping("/api/users/me")
    public UserInfoVO getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }
}
