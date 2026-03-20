package com.example.oauth.user.config;

import com.example.oauth.common.config.ResourceServerSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 用户服务安全配置
 * 导入通用资源服务器配置
 */
@Configuration
@Import(ResourceServerSecurityConfig.class)
public class UserSecurityConfig {
    // 所有配置都在 ResourceServerSecurityConfig 中定义
}
