package com.example.oauth.server.controller; // 定义包路径，用于组织和管理 Java 登录控制器类

import org.springframework.stereotype.Controller; // 导入 Controller 注解，标识此类为 Spring MVC 控制器
import org.springframework.web.bind.annotation.GetMapping; // 导入 GetMapping 注解，映射 HTTP GET 请求到处理方法

/**
 * 登录页面控制器
 * 处理登录相关页面的请求
 */
@Controller // 标识此类为 Spring MVC 控制器，处理 Web 请求
public class LoginController { // 定义登录控制器类

    /**
     * 处理登录页面请求
     * @return String 视图名称（templates/login.html）
     */
    @GetMapping("/login") // 映射 GET 请求到/login 路径
    public String login() { // 处理登录页面请求的方法
        return "login"; // 返回逻辑视图名"login"，对应 templates/login.html
    }

    /**
     * 处理首页请求（授权确认页面）
     * @return String 视图名称（templates/index.html）
     */
    @GetMapping("/") // 映射 GET 请求到根路径
    public String index() { // 处理首页请求的方法
        return "index"; // 返回逻辑视图名"index"，对应 templates/index.html
    }
}
