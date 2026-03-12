package com.example.oauth.server.service; // 定义包路径，用于组织和管理 Java 用户详情服务类

import com.example.oauth.server.entity.Permission; // 导入权限实体类
import com.example.oauth.server.entity.Role; // 导入角色实体类
import com.example.oauth.server.entity.User; // 导入用户实体类
import com.example.oauth.server.mapper.PermissionMapper; // 导入权限 Mapper 接口
import com.example.oauth.server.mapper.RoleMapper; // 导入角色 Mapper 接口
import com.example.oauth.server.mapper.UserMapper; // 导入用户 Mapper 接口
import lombok.RequiredArgsConstructor; // 导入 Lombok 的 RequiredArgsConstructor 注解，自动生成构造函数
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 导入简单授权权限类
import org.springframework.security.core.userdetails.UserDetails; // 导入用户详情接口
import org.springframework.security.core.userdetails.UserDetailsService; // 导入用户详情服务接口
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 导入用户名未找到异常
import org.springframework.stereotype.Service; // 导入 Service 注解，标识此类为 Spring 服务组件

import java.util.ArrayList; // 导入 ArrayList 列表类
import java.util.List; // 导入 List 列表接口

/**
 * 用户详情服务实现类
 * 实现 Spring Security 的 UserDetailsService 接口，负责加载用户信息和权限
 */
@Service // 标识此类为 Spring 服务组件，自动注册到 Spring 容器
@RequiredArgsConstructor // Lombok 注解，生成包含所有 final 字段的构造函数
public class CustomUserDetailsService implements UserDetailsService { // 定义自定义用户详情服务类，实现 UserDetailsService 接口

    private final UserMapper userMapper; // 注入用户 Mapper 接口，用于查询用户信息
    private final RoleMapper roleMapper; // 注入角色 Mapper 接口，用于查询用户角色
    private final PermissionMapper permissionMapper; // 注入权限 Mapper 接口，用于查询用户权限

    /**
     * 根据用户名加载用户详情
     * @param username 用户名
     * @return UserDetails Spring Security 的用户详情对象
     * @throws UsernameNotFoundException 当用户不存在时抛出此异常
     */
    @Override // 实现接口方法
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // 根据用户名加载用户详情的方法
        // 查询用户信息
        User user = userMapper.findByUsername(username); // 通过用户名从数据库查询用户信息
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username); // 如果用户不存在，抛出用户名未找到异常
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用：" + username); // 如果用户状态不是正常状态，抛出异常
        }

        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(user.getId()); // 根据用户 ID 查询所有关联的角色
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(); // 创建权限列表，存储用户的角色和权限

        // 添加角色权限
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode())); // 将角色编码添加到权限列表中，前缀为"ROLE_"
        }

        // 查询用户权限
        List<Permission> permissions = permissionMapper.findByUserId(user.getId()); // 根据用户 ID 查询所有关联的权限
        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getCode())); // 将权限编码添加到权限列表中
        }

        // 返回 Spring Security 的 UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // 设置用户名
                user.getPassword(), // 设置加密后的密码
                authorities // 设置权限列表
        ); // 返回 Spring Security 的 User 对象
    }
}
