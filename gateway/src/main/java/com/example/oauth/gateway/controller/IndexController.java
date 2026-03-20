package com.example.oauth.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 网关首页控制器
 * 返回前端静态页面
 */
@Controller
public class IndexController {

    /**
     * 首页转发到前端 index.html
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
