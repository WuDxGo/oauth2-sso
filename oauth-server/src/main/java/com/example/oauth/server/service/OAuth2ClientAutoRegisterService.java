package com.example.oauth.server.service;

import com.example.oauth.server.repository.JdbcRegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth2 客户端自动注册服务
 * 应用启动时自动注册配置文件中定义的客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ClientAutoRegisterService implements ApplicationRunner {

    private final OAuth2ClientProperties clientProperties;
    private final JdbcRegisteredClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!clientProperties.isAutoRegister()) {
            return;
        }

        // 创建默认客户端（如果已存在则更新）
        createDefaultClients();
    }

    /**
     * 创建默认客户端
     */
    @Transactional
    public void createDefaultClients() {
        List<OAuth2ClientProperties.ClientConfig> defaultClients = clientProperties.getDefaults();

        if (defaultClients == null || defaultClients.isEmpty()) {
            log.warn("未配置默认客户端，跳过自动注册");
            return;
        }

        for (OAuth2ClientProperties.ClientConfig config : defaultClients) {
            registerClient(config);
        }
    }

    /**
     * 注册单个客户端（如果已存在则更新）
     */
    @Transactional
    public void registerClient(OAuth2ClientProperties.ClientConfig config) {
        // 检查客户端是否已存在
        RegisteredClient existingClient = clientRepository.findByClientId(config.getClientId());

        if (existingClient != null) {
            clientRepository.deleteByClientId(config.getClientId());
        }

        // 构建 RegisteredClient
        RegisteredClient.Builder builder = RegisteredClient.withId(java.util.UUID.randomUUID().toString())
                .clientId(config.getClientId())
                .clientName(config.getClientName() != null ? config.getClientName() : config.getClientId())
                .clientSecret(passwordEncoder.encode(config.getClientSecret()));

        // 添加认证方式
        Set<ClientAuthenticationMethod> authMethods = parseCommaSeparatedValues(config.getAuthenticationMethods())
                .stream()
                .map(ClientAuthenticationMethod::new)
                .collect(Collectors.toSet());
        builder.clientAuthenticationMethods(methods -> methods.addAll(authMethods));

        // 添加授权类型
        Set<AuthorizationGrantType> grantTypes = parseCommaSeparatedValues(config.getGrantTypes())
                .stream()
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());
        builder.authorizationGrantTypes(types -> types.addAll(grantTypes));

        // 添加重定向 URI
        if (StringUtils.hasText(config.getRedirectUris())) {
            parseCommaSeparatedValues(config.getRedirectUris())
                    .forEach(builder::redirectUri);
        }

        // 添加 scopes
        parseCommaSeparatedValues(config.getScopes())
                .forEach(builder::scope);

        // 配置 Token 设置
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(config.getAccessTokenTtl()))
                .refreshTokenTimeToLive(Duration.ofSeconds(config.getRefreshTokenTtl()))
                .build();
        builder.tokenSettings(tokenSettings);

        // 配置客户端设置
        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(config.getRequireConsent())
                .build();
        builder.clientSettings(clientSettings);

        // 保存到数据库
        RegisteredClient registeredClient = builder.build();
        clientRepository.save(registeredClient);
    }

    /**
     * 解析逗号分隔的值
     */
    private Set<String> parseCommaSeparatedValues(String values) {
        if (!StringUtils.hasText(values)) {
            return Set.of();
        }
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
