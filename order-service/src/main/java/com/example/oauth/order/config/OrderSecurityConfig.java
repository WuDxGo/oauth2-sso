package com.example.oauth.order.config;

import com.example.oauth.common.config.ResourceServerSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 订单服务安全配置
 * 导入通用资源服务器配置
 */
@Configuration
@Import(ResourceServerSecurityConfig.class)
public class OrderSecurityConfig {
    // 所有配置都在 ResourceServerSecurityConfig 中定义
}
