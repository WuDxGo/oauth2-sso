package com.example.oauth.server.service;

import com.example.oauth.server.entity.Permission;
import com.example.oauth.server.entity.Role;
import com.example.oauth.server.entity.User;
import com.example.oauth.server.mapper.PermissionMapper;
import com.example.oauth.server.mapper.RoleMapper;
import com.example.oauth.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现类
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用：" + username);
        }

        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 添加角色权限
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        }

        // 查询用户权限
        List<Permission> permissions = permissionMapper.findByUserId(user.getId());
        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getCode()));
        }

        // 返回 Spring Security 的 UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
