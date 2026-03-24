package com.example.oauth.server.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 JDBC 的注册客户端仓库实现
 * 将 OAuth2 客户端信息存储在数据库中，支持动态管理
 */
@Slf4j
public class JdbcRegisteredClientRepository implements RegisteredClientRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JdbcRegisteredClientRepository(JdbcTemplate jdbcTemplate) {
        Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null");
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Assert.notNull(registeredClient, "registeredClient cannot be null");
        
        // 先删除旧的，再插入新的（简单实现，生产环境建议用 UPSERT）
        jdbcTemplate.update("DELETE FROM oauth2_registered_client WHERE client_id = ?", 
            registeredClient.getClientId());
        
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
            StringUtils.collectionToDelimitedString(
                registeredClient.getClientAuthenticationMethods(), ","),
            StringUtils.collectionToDelimitedString(
                registeredClient.getAuthorizationGrantTypes(), ","),
            StringUtils.collectionToDelimitedString(
                registeredClient.getRedirectUris(), ","),
            StringUtils.collectionToDelimitedString(
                registeredClient.getScopes(), ","),
            serializeSettings(registeredClient.getClientSettings()),
            serializeSettings(registeredClient.getTokenSettings())
        );
    }

    @Override
    public RegisteredClient findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        
        String sql = "SELECT * FROM oauth2_registered_client WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToRegisteredClient, id);
        } catch (Exception e) {
            return null; // 没有找到返回 null
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Assert.hasText(clientId, "clientId cannot be empty");
        
        String sql = "SELECT * FROM oauth2_registered_client WHERE client_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToRegisteredClient, clientId);
        } catch (Exception e) {
            return null; // 没有找到返回 null
        }
    }

    /**
     * 查询所有客户端
     * @return 客户端列表
     */
    public List<RegisteredClient> findAll() {
        String sql = "SELECT * FROM oauth2_registered_client";
        return jdbcTemplate.query(sql, this::mapRowToRegisteredClient);
    }

    /**
     * 根据客户端 ID 删除客户端
     * @param clientId 客户端 ID
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
        
        Set<ClientAuthenticationMethod> clientAuthenticationMethods = 
            StringUtils.commaDelimitedListToSet(rs.getString("client_authentication_methods"))
                .stream()
                .map(ClientAuthenticationMethod::new)
                .collect(Collectors.toSet());
        
        Set<AuthorizationGrantType> authorizationGrantTypes = 
            StringUtils.commaDelimitedListToSet(rs.getString("authorization_grant_types"))
                .stream()
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());
        
        Set<String> redirectUris = 
            StringUtils.commaDelimitedListToSet(rs.getString("redirect_uris"));
        
        Set<String> scopes =
            StringUtils.commaDelimitedListToSet(rs.getString("scopes"));

        Map<String, Object> clientSettingsMap =
            deserializeSettings(rs.getString("client_settings"));

        Map<String, Object> tokenSettingsMap =
            deserializeSettings(rs.getString("token_settings"));

        // 处理空设置的情况 - 使用默认设置
        ClientSettings clientSettings = clientSettingsMap.isEmpty() 
            ? ClientSettings.builder().build()
            : ClientSettings.withSettings(clientSettingsMap).build();
            
        TokenSettings tokenSettings = tokenSettingsMap.isEmpty()
            ? TokenSettings.builder().build()
            : TokenSettings.withSettings(tokenSettingsMap).build();
        
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
     * 对象转 JSON 字符串
     */
    private String toJson(Object obj) {
        try {
            if (obj == null) {
                return "{}";
            }
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            if (!StringUtils.hasText(json)) {
                // 返回空 Map 而不是 ArrayList
                return (T) new java.util.HashMap<>();
            }
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize JSON to object. JSON: " + json, e);
        }
    }

    /**
     * 将对象序列化为 JSON 字符串（特殊处理 ClientSettings 和 TokenSettings）
     */
    private String serializeSettings(Object settings) {
        if (settings == null) {
            return "{}";
        }
        try {
            // 使用 writeValueAsString 直接序列化对象
            return objectMapper.writeValueAsString(settings);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize settings to JSON", e);
        }
    }

    /**
     * 从 JSON 字符串反序列化为 Map（用于构建 ClientSettings 和 TokenSettings）
     */
    private Map<String, Object> deserializeSettings(String json) {
        // 处理空值情况
        if (!StringUtils.hasText(json) || "{}".equals(json.trim())) {
            return new java.util.HashMap<>();
        }
        try {
            // 直接反序列化为 Map
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            // 记录详细错误日志
            log.warn("反序列化设置失败，JSON: {}, 错误：{}", json, e.getMessage());
            
            // 如果 JSON 格式无效，返回空 Map 而不是抛出异常
            // 这样可以兼容旧数据或损坏的数据
            return new java.util.HashMap<>();
        } catch (Exception e) {
            log.error("反序列化设置时发生未知错误，JSON: {}", json, e);
            return new java.util.HashMap<>();
        }
    }
}
