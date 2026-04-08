package com.example.oauth.server.config;

import com.example.oauth.server.repository.JdbcRegisteredClientRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * OAuth2 认证服务器配置类
 * 负责配置 OAuth2/OIDC 授权服务器的所有核心组件
 * 包括：密钥管理、JWT 编解码器、客户端仓库、安全过滤链等
 */
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    /**
     * JWT 配置属性对象，用于获取颁发者 URL
     */
    private final JwtProperties jwtProperties;

    /**
     * 密钥管理器，负责密钥的生成、加载和持久化
     */
    private final KeyManager keyManager;

    /**
     * 构造函数注入依赖
     *
     * @param jwtProperties JWT 配置属性
     * @param keyManager    密钥管理器
     */
    public AuthorizationServerConfig(JwtProperties jwtProperties, KeyManager keyManager) {
        this.jwtProperties = jwtProperties;
        this.keyManager = keyManager;
    }

    /**
     * RSA 密钥对 Bean
     * 从密钥管理器获取持久化的密钥对，避免每次重启重新生成
     *
     * @return RSA 密钥对
     */
    @Bean
    public KeyPair keyPair() {
        return keyManager.getKeyPair();
    }

    /**
     * RSA 公钥 Bean
     * 用于 JWT 签名验证和 JWK Set 构建
     *
     * @param keyPair RSA 密钥对
     * @return RSA 公钥
     */
    @Bean
    public RSAPublicKey rsaPublicKey(KeyPair keyPair) {
        return (RSAPublicKey) keyPair.getPublic();
    }

    /**
     * RSA 私钥 Bean
     * 用于 JWT 签名
     *
     * @param keyPair RSA 密钥对
     * @return RSA 私钥
     */
    @Bean
    public RSAPrivateKey rsaPrivateKey(KeyPair keyPair) {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    /**
     * JWK（JSON Web Key）源 Bean
     * 用于 OAuth2/OIDC 的 JWK Set 端点（/oauth2/jwks）提供公钥信息
     * 客户端使用此公钥验证 JWT Token 的签名
     *
     * @param keyPair RSA 密钥对
     * @return JWK 源对象
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(KeyPair keyPair) {
        /* 获取公钥用于 JWK 构建 */
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        /* 获取私钥用于 JWK 构建（实际签名时使用） */
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        /* 构建 RSA JWK，包含公钥和私钥，并生成唯一标识符 */
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        /* 将 JWK 包装为不可变集合，供 Spring Security 使用 */
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * JWT 解码器 Bean
     * 用于验证和解码传入的 JWT Token
     * 只使用公钥进行验证，不涉及私钥
     *
     * @param keyPair RSA 密钥对
     * @return JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder
                .withPublicKey((RSAPublicKey) keyPair.getPublic())
                .build();
    }

    /**
     * OAuth2 注册客户端仓库 Bean（主 Bean）
     * 用于 Spring Security OAuth2 框架访问数据库中的客户端配置
     * 使用 @Primary 注解标记为默认注入的 Bean
     *
     * @param jdbcTemplate JDBC 模板，用于数据库访问
     * @return 注册客户端仓库
     */
    @Bean
    @Primary
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * OAuth2 注册客户端仓库 Bean（具体实现类型）
     * 用于服务层需要具体 JdbcRegisteredClientRepository 类型的场景
     * 提供 findByClientId 和 deleteByClientId 等扩展方法
     *
     * @param jdbcTemplate JDBC 模板，用于数据库访问
     * @return JDBC 注册客户端仓库
     */
    @Bean
    public JdbcRegisteredClientRepository jdbcRegisteredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * 授权服务器安全过滤器链 Bean（最高优先级 Order 1）
     * 此过滤链专门处理所有 OAuth2/OIDC 端点请求
     * 优先级设为 1 确保在其他安全过滤链之前执行
     *
     * @param http HttpSecurity 配置对象
     * @return 配置好的安全过滤器链
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        /* 配置此过滤链只匹配 OAuth2/OIDC 标准端点路径 */
        http
            .securityMatcher(
                "/oauth2/authorize",              /* 授权端点（授权码模式） */
                "/oauth2/token",                   /* Token 端点（获取访问令牌） */
                "/oauth2/jwks",                    /* JWK Set 端点（提供公钥） */
                "/oauth2/revoke",                  /* Token 撤销端点 */
                "/oauth2/introspect",              /* Token 内省端点（验证有效性） */
                "/userinfo",                       /* 用户信息端点（OIDC） */
                "/.well-known/openid-configuration" /* OIDC 发现端点 */
            );

        /* 创建 OAuth2 授权服务器配置器，使用默认配置 */
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        /* 逐步配置安全策略 */
        http
            /* 集成 OAuth2 授权服务器配置，启用所有 OAuth2/OIDC 端点 */
            .with(authorizationServerConfigurer, Customizer.withDefaults())

            /* 配置 HTTP 请求授权规则 */
            .authorizeHttpRequests(authorize -> authorize
                /* 允许所有请求访问 Token、JWK Set、OIDC 发现端点（无需认证） */
                .requestMatchers("/oauth2/token", "/oauth2/jwks", "/.well-known/openid-configuration").permitAll()
                /* 其他所有 OAuth2 端点必须经过认证 */
                .anyRequest().authenticated()
            )

            /* 配置表单登录功能，用于授权码模式的用户登录和授权同意 */
            .formLogin(form -> form
                /* 指定自定义登录页面路径 */
                .loginPage("/login")
                /* 指定登录请求处理路径 */
                .loginProcessingUrl("/login")
                /* 允许所有用户访问登录页面和登录接口 */
                .permitAll()
            )

            /* 配置异常处理策略 */
            .exceptionHandling(exceptions -> exceptions
                /* 对 HTML 请求（浏览器访问）配置重定向到登录页面 */
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"), /* 登录页面重定向入口 */
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML) /* 仅匹配 HTML 类型请求 */
                )
            )

            /* 配置 CSRF 防护策略 */
            .csrf(csrf -> csrf
                /* 忽略表单登录接口的 CSRF 检查，防止登录失败 */
                .ignoringRequestMatchers("/api/login")
            );

        /* 构建并返回安全过滤器链 */
        return http.build();
    }

    /**
     * 授权服务器设置 Bean
     * 配置 OAuth2 授权服务器的全局参数
     *
     * @return 授权服务器设置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                /* 设置 JWT 颁发者 URL，必须与客户端配置的一致 */
                .issuer(jwtProperties.getIssuer())
                .build();
    }
}
