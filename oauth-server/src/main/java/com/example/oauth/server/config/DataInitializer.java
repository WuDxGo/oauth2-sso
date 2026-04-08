package com.example.oauth.server.config;

import com.example.oauth.server.entity.Role;
import com.example.oauth.server.entity.User;
import com.example.oauth.server.mapper.RoleMapper;
import com.example.oauth.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化配置类
 * 实现CommandLineRunner接口,在Spring Boot应用启动完成后自动执行
 * 负责创建默认的测试用户和角色,方便开发和测试环境快速使用
 * 仅在所有数据表为空时执行初始化,避免重复创建数据
 * 通过@Component注解注册为Spring Bean,由Spring管理生命周期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /**
     * 用户数据访问接口
     * 用于查询和插入用户数据到数据库
     * 通过构造器注入,由@RequiredArgsConstructor自动生成构造函数
     */
    private final UserMapper userMapper;

    /**
     * 角色数据访问接口
     * 用于查询角色数据和建立用户与角色的关联关系
     */
    private final RoleMapper roleMapper;

    /**
     * 密码编码器
     * 用于对用户密码进行加密存储,防止明文密码泄露
     * 使用BCrypt等强哈希算法,自动加盐防止彩虹表攻击
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Boot应用启动完成后的回调方法
     * 由CommandLineRunner接口定义,在应用上下文完全初始化后自动调用
     * 检查数据库是否已有测试用户,如果没有则创建admin和user两个测试用户
     * 并为它们分配相应的角色,方便立即进行功能测试
     *
     * @param args 命令行参数数组,通常不需要使用
     * @throws Exception 当数据初始化失败时抛出,如数据库连接失败等
     */
    @Override
    public void run(String... args) throws Exception {
        // 步骤1:检查数据库中是否已存在admin用户
        // 如果已存在说明数据已初始化过,直接返回跳过后续逻辑
        User admin = userMapper.findByUsername("admin");

        // 如果admin用户已存在,说明初始化已完成,无需重复执行
        if (admin != null) {
            // 直接返回,跳过数据初始化流程
            return;
        }

        // 步骤2:创建admin管理员用户对象
        User adminUser = new User();
        // 设置用户登录账号为"admin"
        adminUser.setUsername("admin");
        // 设置用户密码,使用PasswordEncoder进行加密后存储
        // 原始密码为"123456",加密后变为BCrypt哈希值
        adminUser.setPassword(passwordEncoder.encode("123456"));
        // 设置用户邮箱地址
        adminUser.setEmail("admin@example.com");
        // 设置用户手机号码
        adminUser.setPhone("13800138000");
        // 设置用户显示昵称为"管理员"
        adminUser.setNickname("管理员");
        // 设置用户性别:1表示男性
        adminUser.setGender(1);
        // 设置用户状态:1表示启用,允许登录系统
        adminUser.setStatus(1);
        // 将admin用户对象插入到数据库的user表中
        userMapper.insert(adminUser);

        // 步骤3:创建user普通用户对象
        User normalUser = new User();
        // 设置用户登录账号为"user"
        normalUser.setUsername("user");
        // 设置用户密码,同样使用"123456"作为原始密码
        normalUser.setPassword(passwordEncoder.encode("123456"));
        // 设置用户邮箱地址
        normalUser.setEmail("user@example.com");
        // 设置用户手机号码
        normalUser.setPhone("13900139000");
        // 设置用户显示昵称为"普通用户"
        normalUser.setNickname("普通用户");
        // 设置用户性别:0表示女性
        normalUser.setGender(0);
        // 设置用户状态:1表示启用,允许登录系统
        normalUser.setStatus(1);
        // 将user用户对象插入到数据库的user表中
        userMapper.insert(normalUser);

        // 步骤4:从数据库中查询ADMIN和USER两个角色对象
        Role adminRole = roleMapper.findByCode("ADMIN");
        Role userRole = roleMapper.findByCode("USER");

        // 步骤5:检查角色是否都已存在,避免空指针异常
        if (adminRole != null && userRole != null) {
            // 为admin用户分配ADMIN和USER两个角色
            // 在user_role关联表中插入记录,建立用户和角色的多对多关系
            roleMapper.addUserRole(adminUser.getId(), adminRole.getId());
            roleMapper.addUserRole(adminUser.getId(), userRole.getId());
            // 为user用户分配USER角色
            roleMapper.addUserRole(normalUser.getId(), userRole.getId());
        }

        // 步骤6:记录数据初始化完成的日志信息
        // 提示测试用户的账号和密码,方便开发人员登录测试
        log.info("测试用户初始化完成: admin/123456, user/123456");
    }
}
