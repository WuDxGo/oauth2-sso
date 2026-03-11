package com.example.oauth.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关安全配置
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges
                // 公开路径
                .pathMatchers("/login**", "/logout**", "/oauth2/**", "/error").permitAll()
                // 其他所有请求需要认证
                .anyExchange().authenticated()
        )
        // 启用 OAuth2 登录
        .oauth2Login(Customizer.withDefaults())
        // 启用登出
        .logout(logout -> logout
                .logoutUrl("/logout")
        )
        // CSRF 配置（网关不需要）
        .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
