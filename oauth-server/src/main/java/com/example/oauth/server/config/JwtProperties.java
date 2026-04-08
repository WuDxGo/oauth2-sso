package com.example.oauth.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 * 用于从application.yml配置文件中读取JWT相关配置参数
 * 通过@ConfigurationProperties注解自动绑定YAML配置到Java对象
 * 配置前缀为"jwt",对应yml中的jwt节点
 * 通过@Component注解注册为Spring Bean,可被其他配置类注入使用
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签发者URL标识
     * 用于标识Token由哪个认证服务器签发
     * 会写入JWT的iss(issuer)声明字段
     * 客户端验证Token时会校验此字段是否匹配
     * 默认值为"http://localhost:8080",即本地开发环境的认证服务器地址
     * 生产环境应改为实际的域名,如"https://auth.example.com"
     */
    private String issuer = "http://localhost:8080";

    /**
     * 访问令牌(Access Token)有效期
     * 单位为秒,定义访问令牌从颁发到失效的时间跨度
     * 默认值为7200秒(即2小时),是安全性和用户体验的平衡值
     * 有效期过短会增加刷新频率,影响性能
     * 有效期过长会降低安全性,Token泄露后风险窗口较大
     * 对应yml中的jwt.access-token-ttl配置项
     */
    private Long accessTokenTtl = 7200L;

    /**
     * 刷新令牌(Refresh Token)有效期
     * 单位为秒,定义刷新令牌从颁发到失效的时间跨度
     * 默认值为604800秒(即7天),允许用户在一周内无需重新登录
     * 刷新令牌用于在访问令牌过期后获取新的访问令牌
     * 客户端可调用/oauth2/token接口传入refresh_token获取新令牌
     * 过期后用户必须重新输入账号密码进行完整登录流程
     * 对应yml中的jwt.refresh-token-ttl配置项
     */
    private Long refreshTokenTtl = 604800L;
}
