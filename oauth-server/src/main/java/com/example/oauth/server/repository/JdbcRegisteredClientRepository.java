package com.example.oauth.server.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于 JDBC 的注册客户端仓库实现
 */
@Slf4j
public class JdbcRegisteredClientRepository implements RegisteredClientRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcRegisteredClientRepository(JdbcTemplate jdbcTemplate) {
        Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null");
        this.jdbcTemplate = jdbcTemplate;
        // 初始化 ObjectMapper 并注册 OAuth2 Authorization Server 的 Jackson 模块
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "registeredClient cannot be null");

        // 先删除旧的，再插入新的
        jdbcTemplate.update("DELETE FROM oauth2_registered_client WHERE client_id = ?",
            registeredClient.getClientId());

        // 提取认证方式的值（而不是 toString）
        String authMethodsStr = registeredClient.getClientAuthenticationMethods().stream()
            .map(m -> m.getValue())
            .collect(Collectors.joining(","));

        // 提取授权类型的值（而不是 toString）
        String grantTypesStr = registeredClient.getAuthorizationGrantTypes().stream()
            .map(g -> g.getValue())
            .collect(Collectors.joining(","));

        // 重定向 URI 和 scopes 直接使用
        String redirectUrisStr = StringUtils.collectionToDelimitedString(registeredClient.getRedirectUris(), ",");
        String scopesStr = StringUtils.collectionToDelimitedString(registeredClient.getScopes(), ",");

        jdbcTemplate.update(
            "INSERT INTO oauth2_registered_client (" +
            "id, client_id, client_name, client_secret, " +
            "client_authentication_methods, authorization_grant_types, " +
            "redirect_uris, scopes, client_settings, token_settings) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            registeredClient.getId(),
            registeredClient.getClientId(),
            registeredClient.getClientName(),
            registeredClient.getClientSecret(),
            authMethodsStr,
            grantTypesStr,
            redirectUrisStr,
            scopesStr,
            serializeClientSettings(registeredClient.getClientSettings()),
            serializeTokenSettings(registeredClient.getTokenSettings())
        );
    }

    @Override
    public RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        String sql = "SELECT * FROM oauth2_registered_client WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToRegisteredClient, id);
        } catch (Exception e) {
            log.warn("根据ID查询客户端失败，ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        String sql = "SELECT * FROM oauth2_registered_client WHERE client_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToRegisteredClient, clientId);
        } catch (Exception e) {
            log.warn("根据ClientId查询客户端失败，ClientId: {}, 错误: {}", clientId, e.getMessage());
            return null;
        }
    }

    /**
     * 查询所有客户端
     */
    public List<RegisteredClient> findAll() {
        String sql = "SELECT * FROM oauth2_registered_client";
        try {
            return jdbcTemplate.query(sql, this::mapRowToRegisteredClient);
        } catch (Exception e) {
            log.error("查询所有客户端失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询所有客户端失败", e);
        }
    }

    /**
     * 根据客户端 ID 删除客户端
     */
    public void deleteByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        String sql = "DELETE FROM oauth2_registered_client WHERE client_id = ?";
        jdbcTemplate.update(sql, clientId);
    }

    /**
     * 将数据库行映射为 RegisteredClient 对象
     */
    private RegisteredClient mapRowToRegisteredClient(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {

        String id = rs.getString("id");
        String clientId = rs.getString("client_id");
        String clientName = rs.getString("client_name");
        String clientSecret = rs.getString("client_secret");

        // 解析认证方式 - 提取实际的字符串值
        Set<ClientAuthenticationMethod> clientAuthenticationMethods = parseStringValue(rs.getString("client_authentication_methods"));

        // 解析授权类型 - 提取实际的字符串值
        Set<AuthorizationGrantType> authorizationGrantTypes = parseAuthorizationGrantTypes(rs.getString("authorization_grant_types"));

        // 解析重定向 URI
        Set<String> redirectUris = StringUtils.commaDelimitedListToSet(rs.getString("redirect_uris"));

        // 解析 scopes
        Set<String> scopes = StringUtils.commaDelimitedListToSet(rs.getString("scopes"));

        // 反序列化设置 - 使用 Builder 模式手动解析
        ClientSettings clientSettings = deserializeClientSettings(rs);
        TokenSettings tokenSettings = deserializeTokenSettings(rs);

        RegisteredClient.Builder builder = RegisteredClient.withId(id)
            .clientId(clientId)
            .clientName(clientName)
            .clientSecret(clientSecret)
            .clientAuthenticationMethods(authMethods -> authMethods.addAll(clientAuthenticationMethods))
            .authorizationGrantTypes(grantTypes -> grantTypes.addAll(authorizationGrantTypes))
            .redirectUris(uris -> uris.addAll(redirectUris))
            .scopes(s -> s.addAll(scopes))
            .clientSettings(clientSettings)
            .tokenSettings(tokenSettings);

        return builder.build();
    }

    /**
     * 解析简单的字符串值集合（处理可能的对象 toString 格式）
     */
    private Set<ClientAuthenticationMethod> parseStringValue(String value) {
        Set<ClientAuthenticationMethod> result = new HashSet<>();
        if (!StringUtils.hasText(value)) {
            return result;
        }
        
        for (String item : StringUtils.commaDelimitedListToSet(value)) {
            // 处理可能的对象 toString 格式，如 "ClientAuthenticationMethod{value='client_secret_basic'}"
            if (item.contains("{value='")) {
                int start = item.indexOf("{value='") + 8;
                int end = item.indexOf("'", start);
                if (end > start) {
                    result.add(new ClientAuthenticationMethod(item.substring(start, end)));
                }
            } else {
                result.add(new ClientAuthenticationMethod(item));
            }
        }
        return result;
    }

    /**
     * 解析授权类型集合
     */
    private Set<AuthorizationGrantType> parseAuthorizationGrantTypes(String value) {
        Set<AuthorizationGrantType> result = new HashSet<>();
        if (!StringUtils.hasText(value)) {
            return result;
        }
        
        for (String item : StringUtils.commaDelimitedListToSet(value)) {
            // 处理可能的对象 toString 格式，如 "AuthorizationGrantType{value='authorization_code'}"
            if (item.contains("{value='")) {
                int start = item.indexOf("{value='") + 8;
                int end = item.indexOf("'", start);
                if (end > start) {
                    result.add(new AuthorizationGrantType(item.substring(start, end)));
                }
            } else {
                result.add(new AuthorizationGrantType(item));
            }
        }
        return result;
    }

    /**
     * 序列化 ClientSettings 为 JSON 字符串
     */
    private String serializeClientSettings(ClientSettings settings) {
        if (settings == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (Exception e) {
            log.error("序列化 ClientSettings 失败", e);
            return "{}";
        }
    }

    /**
     * 序列化 TokenSettings 为 JSON 字符串
     */
    private String serializeTokenSettings(TokenSettings settings) {
        if (settings == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (Exception e) {
            log.error("序列化 TokenSettings 失败", e);
            return "{}";
        }
    }

    /**
     * 从数据库行反序列化 ClientSettings
     */
    private ClientSettings deserializeClientSettings(java.sql.ResultSet rs) {
        try {
            // 直接从 ResultSet 读取布尔值
            boolean requireConsent = false;
            boolean requireProofKey = false;
            
            String clientSettingsStr = rs.getString("client_settings");
            if (StringUtils.hasText(clientSettingsStr) && !clientSettingsStr.trim().equals("{}")) {
                // 尝试从 JSON 中提取值
                if (clientSettingsStr.contains("require-authorization-consent")) {
                    requireConsent = clientSettingsStr.contains("true");
                }
                if (clientSettingsStr.contains("require-proof-key")) {
                    requireProofKey = clientSettingsStr.contains("true");
                }
            }
            
            return ClientSettings.builder()
                .requireAuthorizationConsent(requireConsent)
                .requireProofKey(requireProofKey)
                .build();
        } catch (Exception e) {
            log.warn("反序列化 ClientSettings 失败，使用默认值：{}", e.getMessage());
            return ClientSettings.builder().build();
        }
    }

    /**
     * 从数据库行反序列化 TokenSettings
     */
    private TokenSettings deserializeTokenSettings(java.sql.ResultSet rs) {
        try {
            String tokenSettingsStr = rs.getString("token_settings");
            
            TokenSettings.Builder builder = TokenSettings.builder();
            
            if (StringUtils.hasText(tokenSettingsStr) && !tokenSettingsStr.trim().equals("{}")) {
                // 解析 accessTokenTimeToLive (默认 7200 秒 = 2 小时)
                if (tokenSettingsStr.contains("7200")) {
                    builder.accessTokenTimeToLive(Duration.ofSeconds(7200));
                }
                // 解析 refreshTokenTimeToLive (默认 604800 秒 = 7 天)
                if (tokenSettingsStr.contains("604800")) {
                    builder.refreshTokenTimeToLive(Duration.ofSeconds(604800));
                }
                // 解析 authorizationCodeTimeToLive (默认 300 秒 = 5 分钟)
                if (tokenSettingsStr.contains("300")) {
                    builder.authorizationCodeTimeToLive(Duration.ofSeconds(300));
                }
                // 解析签名算法 (默认 RS256)
                if (tokenSettingsStr.contains("RS256")) {
                    builder.idTokenSignatureAlgorithm(SignatureAlgorithm.RS256);
                }
            }
            
            return builder.build();
        } catch (Exception e) {
            log.warn("反序列化 TokenSettings 失败，使用默认值：{}", e.getMessage());
            return TokenSettings.builder().build();
        }
    }
}
