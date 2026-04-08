package com.example.oauth.server.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Spring Security认证配置类
 * 负责配置认证相关的核心Bean组件
 * 遵循Spring Security 6.4+最佳实践:
 * 1.不手动创建AuthenticationProvider,避免使用已弃用的API
 * 2.让Spring Security自动配置DaoAuthenticationProvider
 * 3.仅提供UserDetailsService和PasswordEncoder这两个必要Bean
 * 通过@Configuration注解注册为配置类,Spring会自动处理Bean的生命周期
 */
@Configuration
public class AuthConfig {

    /**
     * JWK(JSON Web Key)源对象
     * 用于JWT编码时提供RSA密钥进行数字签名
     * 通过构造器注入,由Spring容器管理依赖关系
     */
    private final JWKSource<SecurityContext> jwkSource;

    /**
     * 构造函数注入JWK源依赖
     * Spring会自动调用此构造函数并传入jwkSource Bean
     *
     * @param jwkSource JWK源对象,提供JWT签名所需的密钥
     */
    public AuthConfig(JWKSource<SecurityContext> jwkSource) {
        // 将注入的JWK源赋值给实例变量
        this.jwkSource = jwkSource;
    }

    /**
     * 密码编码器Bean定义方法
     * 使用DelegatingPasswordEncoder委托编码器模式
     * 支持多种加密算法(BCrypt、Pbkdf2、SCrypt等)的自动识别和切换
     * 编码后的密码格式为:{bcrypt}$2a$10$...,前缀标识算法类型
     * 默认使用BCrypt强哈希算法,自动加盐防止彩虹表攻击
     *
     * @return PasswordEncoder密码编码器实例,用于密码加密和验证
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 创建委托密码编码器,支持多种算法自动识别
        // 返回的编码器会根据密码前缀自动选择对应的算法进行验证
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 认证管理器Bean定义方法
     * AuthenticationManager是Spring Security的核心组件
     * 负责协调认证流程,调用AuthenticationProvider执行实际认证
     * Spring Security会自动使用我们定义的UserDetailsService和PasswordEncoder
     * 来创建内部的DaoAuthenticationProvider,无需手动配置
     *
     * @param config AuthenticationConfiguration认证配置对象,由Spring自动注入
     * @return AuthenticationManager认证管理器实例,处理用户登录认证
     * @throws Exception 当获取认证管理器失败时抛出,如配置错误等
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // 从认证配置对象中提取认证管理器
        // 该方法会触发Spring Security的自动配置,创建完整的认证链
        return config.getAuthenticationManager();
    }

    /**
     * JWT编码器Bean定义方法
     * JwtEncoder用于创建和签名JWT Token
     * 使用NimbusJwtEncoder实现,底层使用Nimbus JOSE+JWT库
     * 通过JWK源获取RSA私钥对JWT进行RS256签名算法签名
     * 签名后的Token具有防篡改能力,接收方可通过公钥验证真实性
     *
     * @return JwtEncoder JWT编码器实例,用于生成JWT Token
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        // 创建Nimbus JWT编码器,传入JWK源提供密钥
        // 编码器会使用JWK中的RSA私钥对Token进行数字签名
        return new NimbusJwtEncoder(jwkSource);
    }
}
