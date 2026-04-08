package com.example.oauth.server.config;

import com.example.oauth.server.entity.Role;
import com.example.oauth.server.entity.User;
import com.example.oauth.server.mapper.RoleMapper;
import com.example.oauth.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 数据初始化配置
 * 应用启动时自动创建测试用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有用户
        User admin = userMapper.findByUsername("admin");

        if (admin != null) {
            return;
        }

        // 创建 admin 用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setEmail("admin@example.com");
        adminUser.setPhone("13800138000");
        adminUser.setNickname("管理员");
        adminUser.setGender(1);
        adminUser.setStatus(1);
        userMapper.insert(adminUser);

        // 创建 user 用户
        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setPassword(passwordEncoder.encode("123456"));
        normalUser.setEmail("user@example.com");
        normalUser.setPhone("13900139000");
        normalUser.setNickname("普通用户");
        normalUser.setGender(0);
        normalUser.setStatus(1);
        userMapper.insert(normalUser);

        // 获取角色
        Role adminRole = roleMapper.findByCode("ADMIN");
        Role userRole = roleMapper.findByCode("USER");

        if (adminRole != null && userRole != null) {
            // 关联用户角色
            roleMapper.addUserRole(adminUser.getId(), adminRole.getId());
            roleMapper.addUserRole(adminUser.getId(), userRole.getId());
            roleMapper.addUserRole(normalUser.getId(), userRole.getId());
        }

        // 创建测试订单
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        jdbcTemplate.update("INSERT INTO t_order (order_no, user_id, amount, status, description, create_time, update_time) VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                now + "0001", adminUser.getId(), 100.00, 1, "测试订单 1");
        jdbcTemplate.update("INSERT INTO t_order (order_no, user_id, amount, status, description, create_time, update_time) VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                now + "0002", adminUser.getId(), 200.00, 2, "测试订单 2");
        jdbcTemplate.update("INSERT INTO t_order (order_no, user_id, amount, status, description, create_time, update_time) VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                now + "0003", normalUser.getId(), 150.00, 1, "测试订单 3");
    }
}
