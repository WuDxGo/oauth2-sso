package com.example.oauth.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OAuth2单点登录认证服务器启动类
 * 作为整个应用的入口点,负责初始化Spring Boot应用上下文
 * 自动配置并加载所有组件:控制器、服务、配置、数据访问层等
 * 通过@MapperScan注解扫描MyBatis的Mapper接口,实现数据库操作
 */
@SpringBootApplication
@MapperScan("com.example.oauth.server.mapper")
public class OAuth2ServerApplication {

    /**
     * Java应用程序的标准入口方法
     * 接收命令行参数并启动Spring Boot应用
     * 该方法会阻塞线程直到应用正常关闭
     *
     * @param args 命令行参数数组,可传递启动配置如端口号、环境标识等
     */
    public static void main(String[] args) {
        // 调用SpringApplication的run方法启动应用
        // 第一个参数指定主配置类,用于加载应用上下文
        // 第二个参数传递命令行参数
        // 返回ConfigurableApplicationContext对象代表应用已启动
        SpringApplication.run(OAuth2ServerApplication.class, args);
    }
}
