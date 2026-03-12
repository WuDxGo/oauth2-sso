package com.example.oauth.server.config; // 定义包路径，用于组织和管理 Java OAuth2 服务器配置类

import com.nimbusds.jose.jwk.JWKSet; // 导入 JWK 集合类，用于存储 JSON Web Key
import com.nimbusds.jose.jwk.RSAKey; // 导入 RSA 密钥类，表示 RSA 算法的 JWK
import com.nimbusds.jose.jwk.source.ImmutableJWKSet; // 导入不可变 JWK 源类
import com.nimbusds.jose.jwk.source.JWKSource; // 导入 JWK 源接口
import com.nimbusds.jose.proc.SecurityContext; // 导入安全上下文接口
import org.springframework.context.annotation.Bean; // 导入 Bean 注解，用于标记方法返回的对象将注册为 Spring 容器中的组件
import org.springframework.context.annotation.Configuration; // 导入 Configuration 注解，标识此类为配置类
import org.springframework.core.annotation.Order; // 导入 Order 注解，用于指定配置类的优先级
import org.springframework.http.MediaType; // 导入媒体类型类，用于匹配请求的内容类型
import org.springframework.security.config.Customizer; // 导入 Customizer 工具类，用于简化安全配置
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // 导入 HttpSecurity 构建器，用于配置 Web 安全策略
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // 导入 EnableWebSecurity 注解，启用 Web 安全配置
import org.springframework.security.crypto.factory.PasswordEncoderFactories; // 导入密码编码器工厂类
import org.springframework.security.crypto.password.PasswordEncoder; // 导入密码编码器接口
import org.springframework.security.oauth2.core.AuthorizationGrantType; // 导入授权类型枚举
import org.springframework.security.oauth2.core.ClientAuthenticationMethod; // 导入客户端认证方法枚举
import org.springframework.security.oauth2.core.oidc.OidcScopes; // 导入 OIDC 作用域常量
import org.springframework.security.oauth2.jwt.JwtDecoder; // 导入 JWT 解码器接口
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository; // 导入内存客户端仓库实现
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient; // 导入已注册客户端类
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository; // 导入客户端仓库接口
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer; // 导入 OAuth2 认证服务器配置器
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings; // 导入认证服务器设置类
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings; // 导入客户端设置类
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings; // 导入 Token 设置类
import org.springframework.security.web.SecurityFilterChain; // 导入安全过滤器链接口
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint; // 导入登录 URL 认证入口点
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher; // 导入媒体类型请求匹配器

import java.security.KeyPair; // 导入密钥对类
import java.security.KeyPairGenerator; // 导入密钥对生成器类
import java.security.interfaces.RSAPrivateKey; // 导入 RSA 私钥接口
import java.security.interfaces.RSAPublicKey; // 导入 RSA 公钥接口
import java.time.Duration; // 导入 Duration 类，用于表示时间段
import java.util.UUID; // 导入 UUID 类，用于生成唯一标识符

/**
 * OAuth2 认证服务器配置类
 * 配置 OAuth2 和 OIDC 相关的功能，包括客户端管理、Token 颁发等
 */
@Configuration // 标识此类为 Spring 配置类，相当于 XML 配置文件
@EnableWebSecurity // 启用 Spring Security 的 Web 安全配置功能
public class AuthorizationServerConfig { // 定义 OAuth2 认证服务器配置类

    /**
     * 密码编码器 Bean
     * 使用 Spring Security 提供的委托密码编码器，支持多种编码格式
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public PasswordEncoder passwordEncoder() { // 创建密码编码器方法
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // 创建委托密码编码器，支持{bcrypt}等多种编码格式
    }

    /**
     * RSA 密钥对生成 Bean（用于 JWT 签名和验证）
     * 生成 2048 位的 RSA 密钥对
     * @return KeyPair RSA 密钥对实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public KeyPair keyPair() { // 创建密钥对方法
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); // 获取 RSA 算法的密钥对生成器实例
            keyPairGenerator.initialize(2048); // 初始化密钥对生成器，设置密钥长度为 2048 位
            return keyPairGenerator.generateKeyPair(); // 生成并返回密钥对
        } catch (Exception ex) {
            throw new IllegalStateException("密钥对生成失败", ex); // 如果生成失败，抛出非法状态异常
        }
    }

    /**
     * RSA 公钥 Bean
     * 从密钥对中提取公钥
     * @param keyPair 密钥对实例
     * @return RSAPublicKey RSA 公钥实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public RSAPublicKey rsaPublicKey(KeyPair keyPair) { // 获取 RSA 公钥方法
        return (RSAPublicKey) keyPair.getPublic(); // 从密钥对中提取公钥并强制转换为 RSAPublicKey 类型
    }

    /**
     * RSA 私钥 Bean
     * 从密钥对中提取私钥
     * @param keyPair 密钥对实例
     * @return RSAPrivateKey RSA 私钥实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public RSAPrivateKey rsaPrivateKey(KeyPair keyPair) { // 获取 RSA 私钥方法
        return (RSAPrivateKey) keyPair.getPrivate(); // 从密钥对中提取私钥并强制转换为 RSAPrivateKey 类型
    }

    /**
     * JWK 源配置 Bean
     * 将 RSA 密钥对转换为 JWK（JSON Web Key）格式，用于 OAuth2 发现端点
     * @param keyPair RSA 密钥对实例
     * @return JWKSource<SecurityContext> JWK 源实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public JWKSource<SecurityContext> jwkSource(KeyPair keyPair) { // 创建 JWK 源方法
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic(); // 从密钥对中提取公钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate(); // 从密钥对中提取私钥
        
        RSAKey rsaKey = new RSAKey.Builder(publicKey) // 使用构建器模式创建 RSA 密钥对象
                .privateKey(privateKey) // 设置私钥
                .keyID(UUID.randomUUID().toString()) // 生成唯一的密钥 ID
                .build(); // 构建 RSA 密钥对象
        
        JWKSet jwkSet = new JWKSet(rsaKey); // 创建包含 RSA 密钥的 JWK 集合
        return new ImmutableJWKSet<>(jwkSet); // 返回不可变的 JWK 源，用于提供 JWK 信息
    }

    /**
     * JWT 解码器 Bean
     * 使用 RSA 公钥创建 JWT 解码器，用于验证 JWT Token 的签名
     * @param keyPair RSA 密钥对实例
     * @return JwtDecoder JWT 解码器实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) { // 创建 JWT 解码器方法
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder // 使用 NimbusJwtDecoder 工具类
                .withPublicKey((RSAPublicKey) keyPair.getPublic()) // 使用 RSA 公钥配置解码器
                .build(); // 构建 JWT 解码器实例
    }

    /**
     * 注册客户端仓库 Bean
     * 配置 OAuth2 客户端信息，包括网关、订单服务、用户服务等
     * @param passwordEncoder 密码编码器实例
     * @return RegisteredClientRepository 客户端仓库实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) { // 创建客户端仓库方法
        // 网关客户端配置：用于 OIDC 授权码模式
        RegisteredClient gatewayClient = RegisteredClient.withId(UUID.randomUUID().toString()) // 使用随机 UUID 作为客户端 ID
                .clientId("gateway-client") // 设置客户端 ID 为"gateway-client"
                .clientSecret(passwordEncoder.encode("gateway-secret")) // 设置加密后的客户端密钥
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // 设置客户端认证方式为 BASIC 认证
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // 设置授权类型为授权码模式
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // 支持刷新 Token
                .redirectUri("http://localhost:8081/login/oauth2/code/gateway-client") // 设置回调 URI
                .scope(OidcScopes.OPENID) // 设置 OIDC 的 openid 作用域
                .scope(OidcScopes.PROFILE) // 设置 OIDC 的 profile 作用域
                .scope("read") // 设置读取权限作用域
                .scope("write") // 设置写入权限作用域
                .tokenSettings(TokenSettings.builder() // 配置 Token 相关设置
                        .accessTokenTimeToLive(Duration.ofHours(2)) // 访问令牌有效期 2 小时
                        .refreshTokenTimeToLive(Duration.ofDays(7)) // 刷新令牌有效期 7 天
                        .build()) // 构建 Token 设置对象
                .clientSettings(ClientSettings.builder() // 配置客户端相关设置
                        .requireAuthorizationConsent(true) // 需要用户授权确认
                        .build()) // 构建客户端设置对象
                .build(); // 构建已注册客户端对象

        // 订单服务客户端配置：用于客户端凭证模式（服务间调用）
        RegisteredClient orderClient = RegisteredClient.withId(UUID.randomUUID().toString()) // 使用随机 UUID 作为客户端 ID
                .clientId("order-service") // 设置客户端 ID 为"order-service"
                .clientSecret(passwordEncoder.encode("order-secret")) // 设置加密后的客户端密钥
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // 设置客户端认证方式为 BASIC 认证
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // 设置授权类型为客户端凭证模式
                .scope("read") // 设置读取权限作用域
                .scope("write") // 设置写入权限作用域
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(2)) // 访问令牌有效期 2 小时
                        .refreshTokenTimeToLive(Duration.ofDays(7)) // 刷新令牌有效期 7 天
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true) // 需要用户授权确认
                        .build())
                .build(); // 构建已注册客户端对象

        // 用户服务客户端配置：用于客户端凭证模式（服务间调用）
        RegisteredClient userClient = RegisteredClient.withId(UUID.randomUUID().toString()) // 使用随机 UUID 作为客户端 ID
                .clientId("user-service") // 设置客户端 ID 为"user-service"
                .clientSecret(passwordEncoder.encode("user-secret")) // 设置加密后的客户端密钥
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // 设置客户端认证方式为 BASIC 认证
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) // 设置授权类型为客户端凭证模式
                .scope("read") // 设置读取权限作用域
                .scope("write") // 设置写入权限作用域
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .build())
                .build(); // 构建已注册客户端对象

        return new InMemoryRegisteredClientRepository(gatewayClient, orderClient, userClient); // 返回内存客户端仓库实例，包含所有配置的客户端
    }

    /**
     * 认证服务器安全过滤器链 Bean（优先级 Order 1）
     * 专门处理 OAuth2/OIDC 相关端点的安全配置
     * @param http HttpSecurity 构建器，用于配置 Web 安全策略
     * @return SecurityFilterChain 安全过滤器链实例
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    @Order(1) // 设置优先级为 1，确保优先于其他安全配置（如 DefaultSecurityConfig 的 Order 2）
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception { // 创建认证服务器安全过滤器链方法

        // 1. 【核心修复】限制此过滤器链 ONLY 处理 OAuth2/OIDC 相关端点
        // 解决 UnreachableFilterChainException 的关键
        http.securityMatcher( // 配置此过滤器链只匹配特定的 OAuth2/OIDC 端点
                "/oauth2/authorize", // OAuth2 授权端点
                "/oauth2/token", // OAuth2 Token 端点
                "/oauth2/jwks", // JWK 集合端点
                "/userinfo", // 用户信息端点
                "/.well-known/openid-configuration", // OIDC 发现端点
                "/oauth2/introspect", // Token  introspection 端点
                "/oauth2/revoke", // Token 撤销端点
                "/oauth2/device_authorization", // 设备授权端点
                "/oauth2/device_verification", // 设备验证端点
                "/connect/check_session", // 会话检查端点
                "/connect/endsession", // 结束会话端点
                "/oauth2/consent" // 授权同意端点
        );

        // 2. 【核心修复】注册并获取配置器
        // 第一步：使用 .with() 注册配置器 (替代已弃用的 apply)，确保配置器被初始化
        // 我们不需要接收它的返回值，因为它返回的是 HttpSecurity 本身（用于链式调用）
        http.with(new OAuth2AuthorizationServerConfigurer(), Customizer.withDefaults()); // 使用 with 方法注册 OAuth2 认证服务器配置器

        // 第二步：立即通过 getConfigurer 获取强类型的 OAuth2AuthorizationServerConfigurer 实例
        // 这一步保证了类型安全，解决了 "提供：HttpSecurity" 的编译错误
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = // 获取已注册的 OAuth2 认证服务器配置器实例
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class); // 从 HttpSecurity 中获取指定类型的配置器

        // 如果 getConfigurer 返回 null (理论上不会，因为上面刚 with 过)，可以加个保护，但通常不需要
        if (authorizationServerConfigurer == null) {
            throw new IllegalStateException("OAuth2AuthorizationServerConfigurer not found"); // 如果配置器未找到，抛出异常
        }

        // 3. 启用 OIDC 功能
        authorizationServerConfigurer.oidc(Customizer.withDefaults()); // 启用 OpenID Connect 功能，使用默认配置

        // 4. 配置访问规则
        http.authorizeHttpRequests(authorize -> authorize // 配置 HTTP 请求的授权规则
                .requestMatchers("/oauth2/authorize").authenticated() // 授权端点需要认证
                .requestMatchers("/userinfo").authenticated() // 用户信息端点需要认证
                .requestMatchers("/oauth2/token").permitAll() // Token 端点允许匿名访问
                .requestMatchers("/oauth2/jwks").permitAll() // JWK 端点允许匿名访问
                .requestMatchers("/.well-known/openid-configuration").permitAll() // OIDC 发现端点允许匿名访问
                .anyRequest().authenticated() // 其他所有请求需要认证
        );

        // 5. 表单登录配置
        http.formLogin(Customizer.withDefaults()); // 启用表单登录功能，使用默认配置

        // 6. 异常处理配置
        http.exceptionHandling(exceptions -> exceptions // 配置异常处理
                .defaultAuthenticationEntryPointFor( // 配置默认的认证入口点
                        new LoginUrlAuthenticationEntryPoint("/login"), // 未认证时重定向到/login 页面
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML) // 仅对 HTML 请求生效
                )
        );

        // 7. CSRF 配置
        http.csrf(csrf -> csrf // 配置 CSRF 保护
                .ignoringRequestMatchers("/oauth2/token", "/oauth2/jwks", "/.well-known/openid-configuration") // 忽略 OAuth2 Token、JWK 和发现端点的 CSRF 检查
        );

        return http.build(); // 构建并返回 SecurityFilterChain 实例
    }

    /**
     * 授权服务器设置 Bean
     * 配置 OAuth2 服务器的基本设置，如颁发者 URL
     * @return AuthorizationServerSettings 授权服务器设置实例
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public AuthorizationServerSettings authorizationServerSettings() { // 创建授权服务器设置方法
        return AuthorizationServerSettings.builder() // 使用构建器模式创建授权服务器设置对象
                .issuer("http://localhost:8080") // 设置 Token 的颁发者为 http://localhost:8080
                .build(); // 构建授权服务器设置实例
    }
}
