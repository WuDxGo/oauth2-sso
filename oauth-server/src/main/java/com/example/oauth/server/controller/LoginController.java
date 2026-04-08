package com.example.oauth.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由控制器
 * 负责处理Web页面的路由转发请求
 * 主要返回Thymeleaf模板页面或重定向到静态资源
 * 使用@Controller注解而非@RestController,因为返回的是视图名称而非JSON数据
 */
@Controller
public class LoginController {

    /**
     * 处理用户登录页面的访问请求
     * 当用户访问/login路径时,返回登录页面视图
     *
     * @return 返回Thymeleaf模板文件名"login",对应templates/login.html
     */
    @GetMapping("/login")
    public String login() {
        // 返回视图名称"login"
        // Spring MVC的视图解析器会自动查找templates/login.html
        // Thymeleaf模板引擎负责渲染该页面并返回给浏览器
        return "login";
    }

    /**
     * 处理应用根路径的访问请求
     * 当用户访问应用首页时,将请求转发到前端构建的静态HTML文件
     *
     * @return 使用forward前缀将请求内部转发到/index.html静态资源
     */
    @GetMapping("/")
    public String index() {
        // 使用"forward:"前缀进行服务器端转发
        // 将根路径请求转发到static/index.html或templates/index.html
        // 这样用户访问http://localhost:8080/就能看到前端构建的页面
        return "forward:/index.html";
    }
}
