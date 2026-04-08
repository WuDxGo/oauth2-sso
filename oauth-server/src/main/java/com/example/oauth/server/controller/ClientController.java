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
 * OAuth2客户端管理控制器
 * 提供OAuth2客户端的完整CRUD(增删改查)功能
 * 用于管理注册在该认证服务器上的所有第三方客户端应用
 * 所有接口返回统一的Result响应格式
 *
 * @author OAuth2 Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    /**
     * 客户端数据访问仓库
     * 负责执行客户端信息的数据库操作(增删改查)
     * 通过构造器注入,由@RequiredArgsConstructor自动生成构造函数
     */
    private final JdbcRegisteredClientRepository clientRepository;

    /**
     * 获取所有已注册的OAuth2客户端列表
     * GET请求,无需参数,返回所有客户端的简化信息
     *
     * @return 统一响应对象,data字段为客户端Map列表,包含clientId、clientName等基本信息
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getAllClients() {
        // 从数据库查询所有已注册的客户端信息
        List<RegisteredClient> allClients = clientRepository.findAll();

        // 创建列表用于存储转换后的客户端数据(Map格式)
        List<Map<String, Object>> clients = new ArrayList<>();
        // 遍历所有客户端对象,转换为前端友好的格式
        for (RegisteredClient client : allClients) {
            // 为每个客户端创建一个Map,用于存储键值对数据
            Map<String, Object> clientData = new HashMap<>();
            // 存入客户端唯一标识,如"order-service"、"gateway-client"
            clientData.put("clientId", client.getClientId());
            // 存入客户端显示名称,如"订单服务"、"网关客户端"
            clientData.put("clientName", client.getClientName());
            // 提取授权模式集合,转换为逗号分隔的字符串(如"authorization_code,password")
            clientData.put("authorizationGrantTypes", client.getAuthorizationGrantTypes().stream()
                    .map(AuthorizationGrantType::getValue)      // 提取每个授权模式的字符串值
                    .collect(Collectors.joining(",")));         // 用逗号连接所有模式
            // 提取授权范围集合,转换为逗号分隔的字符串(如"read,write,openid")
            clientData.put("scopes", String.join(",", client.getScopes()));
            // 将转换后的客户端数据添加到列表中
            clients.add(clientData);
        }

        // 返回成功响应,包含客户端列表数据
        return Result.success(clients);
    }

    /**
     * 根据客户端ID查询单个客户端的详细信息
     * GET请求,路径参数为clientId,返回客户端的完整配置信息
     *
     * @param clientId 客户端唯一标识,如"order-service"
     * @return 统一响应对象,data字段为客户端详细信息的Map,包含密钥等敏感信息
     */
    @GetMapping("/{clientId}")
    public Result<Map<String, Object>> getClient(@PathVariable String clientId) {
        // 根据客户端唯一标识从数据库查询完整信息
        RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
        // 如果查询结果为null,说明该客户端不存在,返回错误响应
        if (registeredClient == null) {
            return Result.error("客户端不存在");
        }

        // 创建Map用于存储客户端的详细信息
        Map<String, Object> clientData = new HashMap<>();
        // 存入数据库主键ID(UUID格式)
        clientData.put("id", registeredClient.getId());
        // 存入客户端唯一业务标识
        clientData.put("clientId", registeredClient.getClientId());
        // 存入客户端显示名称
        clientData.put("clientName", registeredClient.getClientName());
        // 存入客户端密钥(加密存储,用于客户端身份验证)
        clientData.put("clientSecret", registeredClient.getClientSecret());
        // 存入授权的所有认证模式集合(如client_secret_basic)
        clientData.put("authorizationGrantTypes", registeredClient.getAuthorizationGrantTypes());
        // 存入授权范围集合(如read、write、openid等)
        clientData.put("scopes", registeredClient.getScopes());
        // 存入所有允许的重定向URI集合(授权码模式回调地址)
        clientData.put("redirectUris", registeredClient.getRedirectUris());

        // 返回成功响应,包含客户端详细信息
        return Result.success(clientData);
    }

    /**
     * 创建新的OAuth2客户端并注册到认证服务器
     * POST请求,接收JSON格式的客户端配置信息
     * 创建后会生成UUID作为数据库主键,并配置默认的Token和客户端设置
     *
     * @param request 客户端创建请求对象,包含clientId、clientName、clientSecret等必填字段
     * @return 统一响应对象,data字段为创建成功后的完整RegisteredClient对象
     */
    @PostMapping
    public Result<RegisteredClient> createClient(@RequestBody ClientRequest request) {
        // 生成全局唯一的数据库主键,使用UUID避免冲突
        String id = UUID.randomUUID().toString();

        // 使用构建器模式创建RegisteredClient对象,逐步设置各项配置
        RegisteredClient.Builder builder = RegisteredClient.withId(id)
                // 设置客户端唯一业务标识,第三方应用通过此标识识别自己
                .clientId(request.getClientId())
                // 设置客户端显示名称,用于管理界面展示
                .clientName(request.getClientName())
                // 设置客户端密钥,会进行加密存储,用于客户端身份验证
                .clientSecret(request.getClientSecret());

        // 处理客户端认证方式列表
        if (request.getClientAuthenticationMethods() != null) {
            // 遍历前端传入的认证方式字符串数组
            for (String method : request.getClientAuthenticationMethods()) {
                // 将每个字符串转换为ClientAuthenticationMethod对象并添加
                builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
            }
        } else {
            // 如果未指定认证方式,默认使用client_secret_basic(基础认证)
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }

        // 处理授权模式列表,决定客户端可以使用哪些OAuth2授权流程
        if (request.getAuthorizationGrantTypes() != null) {
            // 遍历前端传入的授权模式字符串数组
            for (String grantType : request.getAuthorizationGrantTypes()) {
                // 将每个字符串转换为AuthorizationGrantType对象并添加
                builder.authorizationGrantType(new AuthorizationGrantType(grantType));
            }
        }

        // 处理重定向URI列表,授权码模式认证成功后会跳转到这些地址
        if (request.getRedirectUris() != null) {
            // 遍历并重定向URI逐个添加到构建器
            request.getRedirectUris().forEach(builder::redirectUri);
        }

        // 处理授权范围列表,定义客户端可请求的权限范围
        if (request.getScopes() != null) {
            // 遍历并授权范围逐个添加到构建器
            request.getScopes().forEach(builder::scope);
        }

        // 配置Token相关设置,控制Token的生命周期和行为
        TokenSettings tokenSettings = TokenSettings.builder()
                // 设置访问令牌(Access Token)的有效期为2小时(7200秒)
                .accessTokenTimeToLive(Duration.ofHours(2))
                // 设置刷新令牌(Refresh Token)的有效期为7天(604800秒)
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build();
        // 将Token设置应用到客户端构建器
        builder.tokenSettings(tokenSettings);

        // 配置客户端相关设置,控制客户端的授权行为
        ClientSettings clientSettings = ClientSettings.builder()
                // 设置是否需要用户授权同意(首次登录时是否弹出授权确认页面)
                // 从请求中获取,如果为null则默认为false(不需要确认)
                .requireAuthorizationConsent(request.getRequireConsent() != null && request.getRequireConsent())
                .build();
        // 将客户端设置应用到客户端构建器
        builder.clientSettings(clientSettings);

        // 完成构建,生成不可变的RegisteredClient对象
        RegisteredClient registeredClient = builder.build();
        // 将客户端信息保存到数据库
        clientRepository.save(registeredClient);

        // 返回成功响应,包含新创建的客户端对象
        return Result.success("创建成功", registeredClient);
    }

    /**
     * 更新已存在的OAuth2客户端配置
     * PUT请求,路径参数为clientId,接收JSON格式的更新数据
     * 会保持数据库主键ID不变,仅更新业务配置
     *
     * @param clientId 要更新的客户端唯一标识(路径参数)
     * @param request 客户端更新请求对象,包含需要修改的配置字段
     * @return 统一响应对象,data字段为更新后的完整RegisteredClient对象
     */
    @PutMapping("/{clientId}")
    public Result<RegisteredClient> updateClient(
            @PathVariable String clientId,
            @RequestBody ClientRequest request) {

        // 根据客户端ID查询现有客户端,用于获取数据库主键和验证存在性
        RegisteredClient existingClient = clientRepository.findByClientId(clientId);
        // 如果客户端不存在,返回错误响应
        if (existingClient == null) {
            return Result.error("客户端不存在");
        }

        // 使用现有客户端的数据库主键ID重建对象,确保主键不变
        RegisteredClient.Builder builder = RegisteredClient.withId(existingClient.getId())
                // 更新客户端唯一业务标识
                .clientId(request.getClientId())
                // 更新客户端显示名称
                .clientName(request.getClientName())
                // 更新客户端密钥:如果传入新密钥则使用新的,否则保留原密钥
                .clientSecret(request.getClientSecret() != null ? request.getClientSecret() : existingClient.getClientSecret());

        // 处理更新后的客户端认证方式列表
        if (request.getClientAuthenticationMethods() != null) {
            // 遍历并逐个添加新的认证方式
            for (String method : request.getClientAuthenticationMethods()) {
                builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
            }
        } else {
            // 未指定时默认使用client_secret_basic
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        }

        // 处理更新后的授权模式列表
        if (request.getAuthorizationGrantTypes() != null) {
            // 遍历并逐个添加新的授权模式
            for (String grantType : request.getAuthorizationGrantTypes()) {
                builder.authorizationGrantType(new AuthorizationGrantType(grantType));
            }
        }

        // 处理更新后的重定向URI列表
        if (request.getRedirectUris() != null) {
            // 遍历并逐个添加新的重定向URI
            request.getRedirectUris().forEach(builder::redirectUri);
        }

        // 处理更新后的授权范围列表
        if (request.getScopes() != null) {
            // 遍历并逐个添加新的授权范围
            request.getScopes().forEach(builder::scope);
        }

        // 重新配置Token设置,与创建时保持一致
        TokenSettings tokenSettings = TokenSettings.builder()
                // 访问令牌有效期2小时
                .accessTokenTimeToLive(Duration.ofHours(2))
                // 刷新令牌有效期7天
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build();
        // 应用Token设置
        builder.tokenSettings(tokenSettings);

        // 重新配置客户端设置
        ClientSettings clientSettings = ClientSettings.builder()
                // 更新是否需要用户授权同意
                .requireAuthorizationConsent(request.getRequireConsent() != null && request.getRequireConsent())
                .build();
        // 应用客户端设置
        builder.clientSettings(clientSettings);

        // 完成构建,生成更新后的RegisteredClient对象
        RegisteredClient registeredClient = builder.build();
        // 保存更新后的客户端信息到数据库,覆盖原数据
        clientRepository.save(registeredClient);

        // 返回成功响应,包含更新后的客户端对象
        return Result.success("更新成功", registeredClient);
    }

    /**
     * 删除指定的OAuth2客户端
     * DELETE请求,路径参数为clientId
     * 删除后该客户端将无法再进行认证和获取Token
     *
     * @param clientId 要删除的客户端唯一标识
     * @return 统一响应对象,操作成功时data字段为null
     */
    @DeleteMapping("/{clientId}")
    public Result<Void> deleteClient(@PathVariable String clientId) {
        // 先验证客户端是否存在,避免删除不存在的客户端
        RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
        // 如果客户端不存在,返回错误响应
        if (registeredClient == null) {
            return Result.error("客户端不存在");
        }

        // 从数据库中永久删除该客户端及其所有配置
        clientRepository.deleteByClientId(clientId);
        // 返回成功响应
        return Result.success("删除成功", null);
    }

    /**
     * 客户端请求数据传输对象(DTO)
     * 用于接收前端提交的OAuth2客户端配置信息
     * 包含客户端的所有可配置字段
     */
    @lombok.Data
    public static class ClientRequest {
        /**
         * 客户端唯一业务标识
         * 用于在OAuth2流程中识别客户端身份,如"order-service"、"gateway-client"
         * 全局唯一,不可重复
         */
        private String clientId;

        /**
         * 客户端显示名称
         * 用于管理界面展示,方便识别客户端用途,如"订单服务"、"网关客户端"
         */
        private String clientName;

        /**
         * 客户端密钥
         * 用于客户端身份验证时的密码凭证
         * 存储时会进行加密处理(如BCrypt),保障安全性
         */
        private String clientSecret;

        /**
         * 客户端认证方式列表
         * 定义客户端如何向认证服务器证明自己的身份
         * 可选值:client_secret_basic(HTTP基础认证)、client_secret_post(POST参数传递)
         * 如果为null则默认使用client_secret_basic
         */
        private List<String> clientAuthenticationMethods;

        /**
         * 授权模式列表
         * 定义客户端可以使用的OAuth2授权流程类型
         * 可选值:authorization_code(授权码模式)、password(密码模式)、
         *       client_credentials(客户端凭证模式)、refresh_token(刷新令牌)
         * 为空则表示不设置任何授权模式
         */
        private List<String> authorizationGrantTypes;

        /**
         * 重定向URI列表
         * 授权码模式中,用户完成授权后跳转的目标地址
         * 必须是完整的URL格式,如"https://example.com/callback"
         * 用于防止授权码被劫持到恶意网站
         */
        private List<String> redirectUris;

        /**
         * 授权范围列表
         * 定义客户端可以请求的用户权限范围
         * 可选值:openid(OpenID Connect)、profile(用户资料)、read(读权限)、write(写权限)等
         * 为空则表示不限制范围
         */
        private List<String> scopes;

        /**
         * 是否需要用户授权同意标志
         * 控制首次授权时是否需要用户手动确认授权
         * true:首次授权时弹出授权确认页面,用户需手动同意
         * false:自动授权,无需用户确认,直接完成授权流程
         * 为null时默认按false处理
         */
        private Boolean requireConsent;
    }
}
