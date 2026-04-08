package com.example.oauth.server.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2客户端配置属性类
 * 用于从application.yml配置文件中读取客户端相关配置
 * 通过@ConfigurationProperties注解自动绑定YAML配置到Java对象
 * 配置前缀为"oauth2.clients",对应yml中的oauth2.clients节点
 * 支持启动时自动注册配置的默认客户端
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth2.clients")
public class OAuth2ClientProperties {

    /**
     * 是否启用客户端自动注册功能
     * 控制应用启动时是否自动创建配置文件中定义的默认客户端
     * true:启用自动注册,启动时读取defaults列表并创建客户端
     * false:禁用自动注册,需要手动通过API创建客户端
     * 默认值为true,方便开发环境快速部署
     */
    private boolean autoRegister = true;

    /**
     * 默认客户端配置列表
     * 包含多个客户端的配置信息,每个客户端都会在启动时自动注册
     * 列表为空时表示未配置默认客户端,跳过自动注册
     * 对应yml中的oauth2.clients.defaults节点
     */
    private List<ClientConfig> defaults = new ArrayList<>();

    /**
     * 单个OAuth2客户端的配置信息
     * 封装客户端的所有可配置参数,包括身份标识、密钥、授权模式等
     * 作为内部静态类,仅在OAuth2ClientProperties内部使用
     */
    @Data
    public static class ClientConfig {
        /**
         * 客户端唯一业务标识
         * 用于在OAuth2流程中识别客户端身份,如"order-service"、"gateway-client"
         * 全局唯一,不可与其他客户端重复
         * 对应yml中的oauth2.clients.defaults[0].client-id
         */
        private String clientId;

        /**
         * 客户端显示名称
         * 用于管理界面展示,方便识别客户端用途
         * 如"订单服务"、"网关客户端"等中文描述
         * 如果未配置则默认使用clientId作为显示名称
         */
        private String clientName;

        /**
         * 客户端密钥(明文形式)
         * 用于客户端身份验证时的密码凭证
         * 在启动自动注册时会被PasswordEncoder加密后存储到数据库
         * 不应在生产环境的配置文件中明文暴露,建议使用环境变量
         */
        private String clientSecret;

        /**
         * 客户端认证方式列表
         * 多个认证方式用逗号分隔,定义客户端如何向认证服务器证明自己的身份
         * 可选值:client_secret_basic(HTTP Basic认证)、client_secret_post(POST参数传递)
         * 默认值为"client_secret_basic",即使用HTTP Basic认证传递密钥
         * 对应yml中的oauth2.clients.defaults[0].authentication-methods
         */
        private String authenticationMethods = "client_secret_basic";

        /**
         * OAuth2授权模式列表
         * 多个授权模式用逗号分隔,定义客户端可以使用的OAuth2授权流程类型
         * 可选值:authorization_code(授权码模式)、password(密码模式)、
         *       client_credentials(客户端凭证模式)、refresh_token(刷新令牌)
         * 默认值为"client_credentials",即客户端凭证模式,适合服务端到服务端调用
         */
        private String grantTypes = "client_credentials";

        /**
         * 重定向URI列表
         * 多个URI用逗号分隔,授权码模式中用户完成授权后跳转的目标地址
         * 必须是完整的URL格式,如"https://example.com/callback"
         * 用于防止授权码被劫持到恶意网站,保障OAuth2流程安全
         * 该字段仅在授权模式包含authorization_code时生效
         */
        private String redirectUris;

        /**
         * 授权范围列表
         * 多个scope用逗号分隔,定义客户端可以请求的用户权限范围
         * 可选值:openid(OpenID Connect身份认证)、profile(用户资料)、
         *       read(读权限)、write(写权限)等
         * 默认值为"read,write",表示客户端可以请求读和写的权限
         * 客户端在请求授权时可以请求这些scope的子集
         */
        private String scopes = "read,write";

        /**
         * 访问令牌(Access Token)有效期
         * 单位为秒,定义访问令牌从颁发到过期的时间
         * 默认值为7200秒(2小时),过期后客户端需要使用刷新令牌获取新令牌
         * 有效期越短安全性越高,但会增加刷新频率
         */
        private Long accessTokenTtl = 7200L;

        /**
         * 刷新令牌(Refresh Token)有效期
         * 单位为秒,定义刷新令牌从颁发到过期的时间
         * 默认值为604800秒(7天),过期后客户端需要重新进行完整授权流程
         * 刷新令牌用于在访问令牌过期后获取新的访问令牌,无需用户再次登录
         */
        private Long refreshTokenTtl = 604800L;

        /**
         * 是否需要用户授权同意标志
         * 控制首次授权时是否需要用户手动确认授权页面
         * true:首次授权时弹出授权确认页面,用户需手动点击同意
         * false:自动授权,无需用户确认,直接完成授权流程
         * 默认值为false,适合内部系统或信任的客户端,减少用户操作步骤
         */
        private Boolean requireConsent = false;
    }
}
