package com.example.oauth.gateway.config; // 定义包路径，用于组织和管理 Java 网关安全配置类

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关安全配置类
 * 针对 Spring WebFlux（响应式）应用的安全配置
 */
@Configuration // 标识此类为 Spring 配置类，相当于 XML 配置文件
@EnableWebFluxSecurity // 启用 Spring Security 的 WebFlux 响应式 Web 安全配置功能
public class SecurityConfig { // 定义网关安全配置类

    /**
     * 安全过滤器链配置 Bean
     * 定义 HTTP 请求的安全处理规则（响应式版本）
     * @param http ServerHttpSecurity 构建器，用于配置响应式 Web 安全策略
     * @return SecurityWebFilterChain 响应式安全过滤器链实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) { // 创建响应式安全过滤器链方法
        http.authorizeExchange(exchanges -> exchanges // 配置 HTTP 请求的授权规则（响应式 API）
                // 公开路径：登录、登出、OAuth2 相关接口和错误页面允许匿名访问
                .pathMatchers("/login**", "/logout**", "/oauth2/**", "/error").permitAll() // 匹配指定路径的请求，允许所有用户访问
                // 其他所有请求需要认证
                .anyExchange().authenticated() // 除上述路径外的所有请求都需要经过认证
        )
        // 启用 OAuth2 登录功能（用于浏览器访问时的重定向登录）
        .oauth2Login(Customizer.withDefaults())
        // 启用 OAuth2 资源服务器功能，验证 JWT Token（用于 API 调用）
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        // CSRF 配置（网关不需要），因为网关主要负责路由转发和认证
        .csrf(ServerHttpSecurity.CsrfSpec::disable); // 禁用 CSRF 保护，因为网关层主要处理 Token 认证

        return http.build(); // 构建并返回 SecurityWebFilterChain 实例
    }

    /**
     * 响应式 JWT 解码器 Bean
     * 使用 OAuth2 认证服务器的 JWK Set URI 创建 JWT 解码器
     * @return ReactiveJwtDecoder 响应式 JWT 解码器实例
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri("http://localhost:8080/oauth2/jwks")
                .build();
    }
}
