package com.example.oauth.server.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2 客户端配置属性
 * 支持在 application.yml 中配置默认客户端，启动时自动注册
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth2.clients")
public class OAuth2ClientProperties {

    /**
     * 是否启用自动注册
     */
    private boolean autoRegister = true;

    /**
     * 默认客户端列表
     */
    private List<ClientConfig> defaults = new ArrayList<>();

    /**
     * 客户端配置
     */
    @Data
    public static class ClientConfig {
        /**
         * 客户端 ID
         */
        private String clientId;

        /**
         * 客户端名称
         */
        private String clientName;

        /**
         * 客户端密钥（明文，启动时自动加密）
         */
        private String clientSecret;

        /**
         * 认证方式（逗号分隔）
         */
        private String authenticationMethods = "client_secret_basic";

        /**
         * 授权类型（逗号分隔）
         */
        private String grantTypes = "client_credentials";

        /**
         * 重定向 URI（逗号分隔）
         */
        private String redirectUris;

        /**
         * 授权范围（逗号分隔）
         */
        private String scopes = "read,write";

        /**
         * Token 过期时间（秒）
         */
        private Long accessTokenTtl = 7200L;

        /**
         * 刷新 Token 过期时间（秒）
         */
        private Long refreshTokenTtl = 604800L;

        /**
         * 是否需要授权同意
         */
        private Boolean requireConsent = false;
    }
}
