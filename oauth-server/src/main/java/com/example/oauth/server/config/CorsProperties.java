package com.example.oauth.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    
    /**
     * 允许的源列表
     */
    private List<String> allowedOrigins;
    
    /**
     * 允许的HTTP方法
     */
    private List<String> allowedMethods;
    
    /**
     * 预检请求缓存时间（秒）
     */
    private Long maxAge = 3600L;
}
