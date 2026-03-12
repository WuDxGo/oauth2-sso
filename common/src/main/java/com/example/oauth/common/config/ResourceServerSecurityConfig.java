package com.example.oauth.common.config; // 定义包路径，用于组织和管理 Java 类

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass; // 导入条件注解，当类路径下存在指定类时生效
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; // 导入条件注解，根据配置文件中的属性值决定是否加载此配置
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties; // 导入 OAuth2 资源服务器配置属性类
import org.springframework.context.annotation.Bean; // 导入 Bean 注解，用于标记方法返回的对象将注册为 Spring 容器中的组件
import org.springframework.context.annotation.Configuration; // 导入 Configuration 注解，标识此类为配置类
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 导入方法级安全配置注解
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // 导入 HttpSecurity 构建器，用于配置 Web 安全策略
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // 导入 EnableWebSecurity 注解，启用 Web 安全配置
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // 导入抽象 HTTP 配置器，用于禁用 CSRF 等功能
import org.springframework.security.config.http.SessionCreationPolicy; // 导入会话创建策略枚举，定义会话管理方式
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter; // 导入 JWT 认证转换器
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter; // 导入 JWT 授权转换器
import org.springframework.security.web.SecurityFilterChain; // 导入安全过滤器链接口，定义一系列安全过滤规则

/**
 * 通用的资源服务器安全配置类
 * 适用于所有需要 JWT 认证的资源服务（如 user-service, order-service 等）
 * 
 * 配置特性：
 * 1. 启用 JWT 资源服务器
 * 2. 配置 JWT 认证转换器（从 scope 提取权限）
 * 3. 无状态会话
 * 4. 禁用 CSRF（JWT 不需要）
 * 5. /actuator/** 和 /health 公开
 */
@Configuration // 标识此类为 Spring 配置类，相当于 XML 配置文件
@ConditionalOnClass(name = "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken") // 当类路径下存在 JwtAuthenticationToken 类时启用此配置
@EnableWebSecurity // 启用 Spring Security 的 Web 安全配置功能
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级安全控制，prePostEnabled=true 表示支持@PreAuthorize 和@PostAuthorize 注解
public class ResourceServerSecurityConfig { // 定义资源服务器安全配置类

    /**
     * JWT 认证转换器 Bean
     * 将 JWT 中的 scope 声明转换为 Spring Security 的权限（GrantedAuthority）
     * @return JwtAuthenticationConverter JWT 认证转换器实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() { // 创建 JWT 认证转换器方法
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter(); // 创建 JWT 授权转换器实例，用于从 JWT 中提取权限信息
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // 设置权限前缀为"ROLE_"，Spring Security 会在权限前自动添加此前缀
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope"); // 设置权限声明的名称为"scope"，从 JWT 的 scope 字段中提取权限

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter(); // 创建 JWT 认证转换器实例
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter); // 将授权转换器设置到认证转换器中
        return jwtAuthenticationConverter; // 返回配置好的 JWT 认证转换器实例
    }

    /**
     * 安全过滤器链配置 Bean
     * 定义 HTTP 请求的安全处理规则
     * <p>
     * 默认配置：
     * - /actuator/** 和 /health 允许匿名访问
     * - 其他所有请求需要认证
     * </p>
     * @param http HttpSecurity 构建器，用于配置 Web 安全策略
     * @return SecurityFilterChain 安全过滤器链实例
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // 创建安全过滤器链方法
        http.authorizeHttpRequests(authorize -> authorize // 配置 HTTP 请求的授权规则
                // 公开接口（健康检查、监控端点），允许匿名访问
                .requestMatchers("/actuator/**", "/health").permitAll() // 匹配/actuator/**和/health 路径的请求，允许所有用户访问
                // 其他所有请求需要认证
                .anyRequest().authenticated() // 除上述路径外的所有请求都需要经过认证
        )
        // 启用 JWT 资源服务器，配置使用 JWT 进行认证
        .oauth2ResourceServer(oauth2 -> oauth2 // 配置 OAuth2 资源服务器功能
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())) // 配置 JWT 认证转换器，使用上面定义的 jwtAuthenticationConverter 方法
        )
        // 无状态会话，不使用 HTTP Session
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 配置会话管理策略为 STATELESS（无状态），每次请求都不创建 Session
        // CSRF 禁用（使用 JWT 不需要），因为 JWT 本身已经提供了足够的安全性
        .csrf(AbstractHttpConfigurer::disable); // 禁用 CSRF 保护，因为 JWT Token 已经提供了足够的安全性

        return http.build(); // 构建并返回 SecurityFilterChain 实例
    }
}
