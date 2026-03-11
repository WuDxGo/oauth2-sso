package com.example.oauth.server;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt 密码生成测试工具
 */
public class PasswordTest {

    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 生成 123456 的 BCrypt 哈希
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt 加密后: " + encodedPassword);
        System.out.println();
        
        // 验证密码是否匹配
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 输出 SQL 语句
        System.out.println();
        System.out.println("SQL 更新语句:");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username IN ('admin', 'user');");
    }
}
