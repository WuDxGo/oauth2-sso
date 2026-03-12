package com.example.oauth.user; // 定义包路径，用于组织和管理 Java 用户服务应用类

import org.mybatis.spring.annotation.MapperScan; // 导入 MyBatis 的 MapperScan 注解，用于扫描 Mapper 接口
import org.springframework.boot.SpringApplication; // 导入 Spring Boot 启动类，用于启动 Spring Boot 应用
import org.springframework.boot.autoconfigure.SpringBootApplication; // 导入 Spring Boot 自动配置注解，启用组件扫描和自动配置

/**
 * 用户服务启动类
 * 使用@SpringBootApplication 注解标识，是 Spring Boot 应用的入口点
 */
@SpringBootApplication // Spring Boot 核心注解，标识此类为配置类并启用自动配置和组件扫描
@MapperScan("com.example.oauth.user.mapper") // MyBatis 注解，指定要扫描的 Mapper 接口包路径
public class UserServiceApplication { // 定义用户服务应用主类

    /**
     * 应用程序主方法（入口点）
     * @param args 命令行参数数组
     */
    public static void main(String[] args) { // 程序入口方法
        SpringApplication.run(UserServiceApplication.class, args); // 启动 Spring Boot 应用，加载配置并初始化上下文
    }
}
