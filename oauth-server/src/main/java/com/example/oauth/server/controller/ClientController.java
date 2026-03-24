package com.example.oauth.server.controller;

import com.example.oauth.common.result.Result;
import com.example.oauth.server.repository.JdbcRegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OAuth2 客户端管理控制器
 * 提供 OAuth2 客户端的增删改查（CRUD）功能
 * 
 * @author OAuth2 Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    /**
     * 客户端仓库，用于操作数据库中的客户端数据
     */
    private final JdbcRegisteredClientRepository clientRepository;

    /**
     * 获取所有已注册的 OAuth2 客户端列表
     * 
     * @return 包含所有客户端信息的 Result 对象，data 字段为客户端列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getAllClients() {
        // 从数据库查询所有客户端
        List<RegisteredClient> allClients = clientRepository.findAll();
        
        // 将 RegisteredClient 转换为前端友好的 Map 格式
        List<Map<String, Object>> clients = new ArrayList<>();
        for (RegisteredClient client : allClients) {
            Map<String, Object> clientData = new HashMap<>();
            // 客户端唯一标识，如：order-service
            clientData.put("clientId", client.getClientId());
            // 客户端显示名称，如：订单服务
            clientData.put("clientName", client.getClientName());
            // 授权模式列表，转换为逗号分隔的字符串
            clientData.put("authorizationGrantTypes", client.getAuthorizationGrantTypes().stream()
                    .map(AuthorizationGrantType::getValue)
                    .collect(Collectors.joining(",")));
            // 授权范围列表，转换为逗号分隔的字符串
            clientData.put("scopes", String.join(",", client.getScopes()));
            clients.add(clientData);
        }

        return Result.success(clients);
    }

    /**
     * 根据客户端 ID 查询单个客户端详情
     * 
     * @param clientId 客户端唯一标识
     * @return 包含客户端详细信息的 Result 对象
     */
    @GetMapping("/{clientId}")
    public Result<Map<String, Object>> getClient(@PathVariable String clientId) {
        // 根据客户端 ID 查询客户端信息
        RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            return Result.error("客户端不存在");
        }

        // 构建客户端详情数据
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("id", registeredClient.getId());
        clientData.put("clientId", registeredClient.getClientId());
        clientData.put("clientName", registeredClient.getClientName());
        clientData.put("clientSecret", registeredClient.getClientSecret());
        clientData.put("authorizationGrantTypes", registeredClient.getAuthorizationGrantTypes());
        clientData.put("scopes", registeredClient.getScopes());
        clientData.put("redirectUris", registeredClient.getRedirectUris());

        return Result.success(clientData);
    }

    /**
     * 创建新的 OAuth2 客户端
     * 
     * @param request 客户端创建请求参数
     * @return 包含创建后客户端信息的 Result 对象
     */
    @PostMapping
    public Result<RegisteredClient> createClient(@RequestBody ClientRequest request) {
        // 生成唯一的客户端 ID（数据库主键）
        String id = UUID.randomUUID().toString();

        // 使用构建器模式构建 RegisteredClient 对象
        RegisteredClient.Builder builder = RegisteredClient.withId(id)
                // 设置客户端唯一标识（如：order-service）
                .clientId(request.getClientId())
                // 设置客户端显示名称（如：订单服务）
                .clientName(request.getClientName())
                // 设置客户端密钥（加密存储）
                .clientSecret(request.getClientSecret());

        // 添加认证方式（支持多种认证方式）
        if (request.getClientAuthenticationMethods() != null) {
            for (String method : request.getClientAuthenticationMethods()) {
                builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
            }
        } else {
            // 默认使用 client_secret_basic 认证方式
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }

        // 添加授权模式（支持多种授权模式）
        if (request.getAuthorizationGrantTypes() != null) {
            for (String grantType : request.getAuthorizationGrantTypes()) {
                builder.authorizationGrantType(new AuthorizationGrantType(grantType));
            }
        }

        // 添加重定向 URI 列表（授权成功后跳转的地址）
        if (request.getRedirectUris() != null) {
            request.getRedirectUris().forEach(builder::redirectUri);
        }

        // 添加授权范围（如：read, write, openid 等）
        if (request.getScopes() != null) {
            request.getScopes().forEach(builder::scope);
        }

        // 配置 Token 设置
        TokenSettings tokenSettings = TokenSettings.builder()
                // 访问令牌有效期：2 小时（7200 秒）
                .accessTokenTimeToLive(Duration.ofHours(2))
                // 刷新令牌有效期：7 天（604800 秒）
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build();
        builder.tokenSettings(tokenSettings);

        // 配置客户端设置
        ClientSettings clientSettings = ClientSettings.builder()
                // 是否需要用户授权同意（首次授权时需要用户确认）
                .requireAuthorizationConsent(request.getRequireConsent() != null && request.getRequireConsent())
                .build();
        builder.clientSettings(clientSettings);

        // 构建并保存客户端
        RegisteredClient registeredClient = builder.build();
        clientRepository.save(registeredClient);

        return Result.success("创建成功", registeredClient);
    }

    /**
     * 更新已存在的 OAuth2 客户端配置
     * 
     * @param clientId 客户端唯一标识（路径参数）
     * @param request 客户端更新请求参数
     * @return 包含更新后客户端信息的 Result 对象
     */
    @PutMapping("/{clientId}")
    public Result<RegisteredClient> updateClient(
            @PathVariable String clientId,
            @RequestBody ClientRequest request) {

        // 先根据客户端 ID 查找现有客户端
        RegisteredClient existingClient = clientRepository.findByClientId(clientId);
        if (existingClient == null) {
            return Result.error("客户端不存在");
        }

        // 使用现有客户端的 ID 重建客户端（保持数据库主键不变）
        RegisteredClient.Builder builder = RegisteredClient.withId(existingClient.getId())
                .clientId(request.getClientId())
                .clientName(request.getClientName())
                // 如果新密钥为空则保留原密钥，否则使用新密钥
                .clientSecret(request.getClientSecret() != null ? request.getClientSecret() : existingClient.getClientSecret());

        // 添加认证方式
        if (request.getClientAuthenticationMethods() != null) {
            for (String method : request.getClientAuthenticationMethods()) {
                builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
            }
        } else {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }

        // 添加授权类型
        if (request.getAuthorizationGrantTypes() != null) {
            for (String grantType : request.getAuthorizationGrantTypes()) {
                builder.authorizationGrantType(new AuthorizationGrantType(grantType));
            }
        }

        // 添加重定向 URI
        if (request.getRedirectUris() != null) {
            request.getRedirectUris().forEach(builder::redirectUri);
        }

        // 添加授权范围
        if (request.getScopes() != null) {
            request.getScopes().forEach(builder::scope);
        }

        // 配置 Token 设置（与创建时相同）
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(2))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build();
        builder.tokenSettings(tokenSettings);

        // 配置客户端设置
        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(request.getRequireConsent() != null && request.getRequireConsent())
                .build();
        builder.clientSettings(clientSettings);

        // 构建并保存更新后的客户端
        RegisteredClient registeredClient = builder.build();
        clientRepository.save(registeredClient);

        return Result.success("更新成功", registeredClient);
    }

    /**
     * 删除指定的 OAuth2 客户端
     * 
     * @param clientId 客户端唯一标识
     * @return 操作结果
     */
    @DeleteMapping("/{clientId}")
    public Result<Void> deleteClient(@PathVariable String clientId) {
        // 先检查客户端是否存在
        RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            return Result.error("客户端不存在");
        }

        // 从数据库中删除客户端
        clientRepository.deleteByClientId(clientId);
        return Result.success("删除成功", null);
    }

    /**
     * 客户端请求数据传输对象（DTO）
     * 用于接收前端提交的客户端配置信息
     */
    @lombok.Data
    public static class ClientRequest {
        /**
         * 客户端唯一标识（如：order-service, gateway-client）
         */
        private String clientId;
        
        /**
         * 客户端显示名称（如：订单服务，网关客户端）
         */
        private String clientName;
        
        /**
         * 客户端密钥（用于客户端认证，加密存储）
         */
        private String clientSecret;
        
        /**
         * 客户端认证方式列表
         * 可选值：client_secret_basic（推荐）、client_secret_post
         */
        private List<String> clientAuthenticationMethods;
        
        /**
         * 授权模式列表
         * 可选值：authorization_code（授权码模式）、password（密码模式）、
         *        client_credentials（客户端凭证模式）、refresh_token（刷新 Token）
         */
        private List<String> authorizationGrantTypes;
        
        /**
         * 重定向 URI 列表（授权成功后跳转的地址）
         */
        private List<String> redirectUris;
        
        /**
         * 授权范围列表
         * 可选值：openid、profile、read、write 等
         */
        private List<String> scopes;
        
        /**
         * 是否需要用户授权同意
         * true: 首次授权时需要用户手动确认
         * false: 自动授权，无需用户确认
         */
        private Boolean requireConsent;
    }
}
