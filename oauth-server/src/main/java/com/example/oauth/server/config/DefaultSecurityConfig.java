package com.example.oauth.server.config; // 定义包路径，用于组织和管理 Java 默认安全配置类

import org.springframework.context.annotation.Bean; // 导入 Bean 注解，用于标记方法返回的对象将注册为 Spring 容器中的组件
import org.springframework.context.annotation.Configuration; // 导入 Configuration 注解，标识此类为配置类
import org.springframework.core.annotation.Order; // 导入 Order 注解，用于指定配置类的优先级
import org.springframework.http.MediaType; // 导入媒体类型类，用于匹配请求的内容类型
import org.springframework.security.config.Customizer; // 导入 Customizer 工具类，用于简化安全配置
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // 导入 HttpSecurity 构建器，用于配置 Web 安全策略
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // 导入 EnableWebSecurity 注解，启用 Web 安全配置
import org.springframework.security.web.SecurityFilterChain; // 导入安全过滤器链接口
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint; // 导入登录 URL 认证入口点
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher; // 导入媒体类型请求匹配器

/**
 * 默认安全配置类
 * 处理普通页面登录、静态资源访问等功能
 * 优先级低于 OAuth2 服务器配置 (Order 2 > Order 1)
 */
@Configuration // 标识此类为 Spring 配置类，相当于 XML 配置文件
@EnableWebSecurity // 启用 Spring Security 的 Web 安全配置功能
@Order(2) // 设置优先级为 2，确保低于 AuthorizationServerConfig 的 Order 1
public class DefaultSecurityConfig { // 定义默认安全配置类

    /**
     * 默认安全过滤器链 Bean
     * 处理非 OAuth2 相关的安全配置
     * @param http HttpSecurity 构建器，用于配置 Web 安全策略
     * @return SecurityFilterChain 安全过滤器链实例
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean // 标记此方法返回的对象将注册为 Spring 容器中的 Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception { // 创建默认安全过滤器链方法
        http
                // 1. 授权规则配置
                .authorizeHttpRequests(authorize -> authorize // 配置 HTTP 请求的授权规则
                        // 静态资源公开访问
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll() // CSS、JS、图片、图标等静态资源允许匿名访问
                        // 登录、登出、错误页面接口公开
                        .requestMatchers("/login", "/logout", "/error").permitAll() // 登录、登出和错误页面允许匿名访问
                        // 其他所有请求需要认证
                        .anyRequest().authenticated() // 除上述路径外的所有请求都需要经过认证
                )
                // 2. 表单登录配置
                .formLogin(formLogin -> formLogin // 配置表单登录功能
                        .loginPage("/login") // 设置自定义登录页面 URL 为/login
                        .permitAll() // 允许所有人访问登录页
                        .defaultSuccessUrl("/", true) // 登录成功后默认跳转到首页
                        .failureUrl("/login?error=true") // 登录失败后重定向到登录页并显示错误信息
                )
                // 3. 登出配置
                .logout(logout -> logout // 配置登出功能
                        .logoutUrl("/logout") // 设置登出 URL 为/logout
                        .logoutSuccessUrl("/login?logout=true") // 登出成功后重定向到登录页并显示成功信息
                        .invalidateHttpSession(true) // 登出时使 HTTP Session 失效
                        .deleteCookies("JSESSIONID") // 删除 JSESSIONID Cookie
                )
                // 4. 异常处理配置（未认证重定向）
                .exceptionHandling(exceptions -> exceptions // 配置异常处理
                        .defaultAuthenticationEntryPointFor( // 配置默认的认证入口点
                                new LoginUrlAuthenticationEntryPoint("/login"), // 未认证时重定向到/login 页面
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML) // 仅对 HTML 请求生效
                        )
                )
                // 5. CSRF 配置（根据需求开启或关闭，默认是开启的）
                // 如果是前后端分离或纯 API，可能需要 csrf().disable()
                // 这里保持默认开启以保护表单提交
                .csrf(Customizer.withDefaults()); // 启用 CSRF 保护，使用默认配置

        return http.build(); // 构建并返回 SecurityFilterChain 实例
    }
}