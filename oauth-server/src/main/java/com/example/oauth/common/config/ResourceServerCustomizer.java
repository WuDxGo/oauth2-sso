package com.example.oauth.common.config; // 定义包路径，用于组织和管理 Java 类

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; // 导入条件注解，根据配置文件中的属性值决定是否加载此配置
import org.springframework.context.annotation.Bean; // 导入 Bean 注解，用于标记方法返回的对象将注册为 Spring 容器中的组件
import org.springframework.context.annotation.Configuration; // 导入 Configuration 注解，标识此类为配置类
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // 导入 HttpSecurity 构建器，用于配置 Web 安全策略
import org.springframework.security.web.SecurityFilterChain; // 导入安全过滤器链接口，定义一系列安全过滤规则

/**
 * 资源服务器自定义配置助手类
 * 当某个服务需要自定义安全配置时，可以通过配置类继承或覆盖此配置
 * 
 * 使用方式说明：
 * 1. 默认配置（推荐）：不需要任何操作，自动生效
 * 2. 自定义路径：创建自己的 SecurityConfig，覆盖默认的 securityFilterChain
 * 3. 通过 application.yml 配置：common.security.public-paths 可自定义公开路径
 */
@Configuration // 标识此类为 Spring 配置类，相当于 XML 配置文件
@ConditionalOnProperty(name = "common.security.enabled", havingValue = "true", matchIfMissing = true) // 当配置文件中 common.security.enabled 为 true 或缺省时启用此配置
public class ResourceServerCustomizer { // 定义资源服务器自定义配置类

    /**
     * 如果需要自定义配置，可以创建一个新的 SecurityFilterChain Bean
     * 并指定更高的优先级
     * 
     * 示例代码：
     * @Bean
     * @Order(1) // 更高的优先级，数字越小优先级越高
     * public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
     *     // 自定义配置逻辑
     * }
     */
    
    /**
     * 配置说明：
     * - 默认公开路径：/actuator/**, /health
     * - 默认所有请求需要认证
     * - JWT token 验证
     * - scope 转换为权限
     */
}
