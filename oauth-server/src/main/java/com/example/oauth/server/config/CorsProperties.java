package com.example.oauth.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS(跨域资源共享)配置属性类
 * 用于从application.yml配置文件中读取跨域策略配置
 * CORS是一种浏览器安全机制,允许或拒绝不同域名之间的HTTP请求
 * 通过@ConfigurationProperties注解自动绑定YAML配置到Java对象
 * 配置前缀为"cors",对应yml中的cors节点
 * 通过@Component注解注册为Spring Bean,可被安全配置类注入使用
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * 允许跨域请求的源(域名)列表
     * 源指的是前端应用的完整URL,包括协议和端口
     * 如["http://localhost:5173","https://example.com"]
     * 只有列表中的域名才能向后端发送跨域请求
     * 设置为"*"表示允许任意域名访问(不推荐,存在安全风险)
     * 对应yml中的cors.allowed-origins配置项
     */
    private List<String> allowedOrigins;

    /**
     * 允许的HTTP请求方法列表
     * 定义哪些HTTP方法可以通过跨域请求发送
     * 如["GET","POST","PUT","DELETE","OPTIONS"]
     * OPTIONS方法用于CORS预检请求(preflight request)
     * 默认应包含常用的CRUD操作方法
     * 对应yml中的cors.allowed-methods配置项
     */
    private List<String> allowedMethods;

    /**
     * 预检请求(Preflight Request)缓存时间
     * 单位为秒,定义浏览器缓存CORS预检检查结果的时间
     * 默认值为3600秒(即1小时)
     * 预检请求是浏览器在发送复杂跨域请求前自动发送的OPTIONS请求
     * 用于确认服务器是否允许该跨域请求
     * 缓存预检结果可减少OPTIONS请求的发送频率,提高性能
     * 对应yml中的cors.max-age配置项
     */
    private Long maxAge = 3600L;
}
