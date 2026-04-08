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
 * 自定义用户详情服务实现类
 * 实现Spring Security的UserDetailsService接口,负责从数据库加载用户完整信息
 * 包括用户基本信息、角色列表和权限列表,用于Spring Security的认证和授权流程
 * 通过@Service注解注册为Spring Bean,由Spring管理生命周期
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * 用户数据访问接口
     * 用于根据用户名查询用户基本信息(用户名、密码、状态等)
     * 通过构造器注入,由@RequiredArgsConstructor自动生成构造函数
     */
    private final UserMapper userMapper;

    /**
     * 角色数据访问接口
     * 用于根据用户ID查询该用户关联的所有角色
     * 角色是多对多关系,一个用户可以拥有多个角色
     */
    private final RoleMapper roleMapper;

    /**
     * 权限数据访问接口
     * 用于根据用户ID查询该用户直接关联的所有权限
     * 权限用于细粒度的访问控制
     */
    private final PermissionMapper permissionMapper;

    /**
     * 根据用户名加载用户详情信息
     * 该方法由Spring Security在用户认证时自动调用
     * 负责从数据库查询用户信息并转换为Spring Security能识别的格式
     *
     * @param username 用户名字符串,由登录接口传入
     * @return UserDetails对象,包含用户名、加密密码和权限列表,供Spring Security验证
     * @throws UsernameNotFoundException 当用户不存在或已被禁用时抛出,阻止认证流程
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 步骤1:根据用户名从数据库查询用户基本信息(用户名、密码、状态等)
        // UserMapper通过MyBatis执行SQL查询返回User实体对象
        User user = userMapper.findByUsername(username);
        // 如果查询结果为null,说明该用户不存在,抛出异常阻止认证
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 步骤2:检查用户的启用状态,确保只有活跃用户可以登录
        // status字段:1表示启用,0或其他值表示禁用
        if (user.getStatus() != 1) {
            // 用户已禁用,抛出异常并提示明确原因
            throw new UsernameNotFoundException("用户已被禁用：" + username);
        }

        // 步骤3:根据用户ID查询该用户关联的所有角色
        // 角色是多对多关系,通过中间表关联
        List<Role> roles = roleMapper.findByUserId(user.getId());
        // 创建Spring Security的权限列表,用于存储角色和权限信息
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 步骤4:遍历角色列表,将每个角色转换为Spring Security的权限对象
        for (Role role : roles) {
            // 角色权限需要添加"ROLE_"前缀,这是Spring Security的命名约定
            // 例如角色code为"ADMIN",转换为"ROLE_ADMIN"
            // 这样可以在安全表达式中使用hasRole('ADMIN')进行权限校验
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        }

        // 步骤5:根据用户ID查询该用户直接关联的所有权限(细粒度权限)
        // 权限不同于角色,是更细粒度的访问控制,如"user:read"、"user:write"
        List<Permission> permissions = permissionMapper.findByUserId(user.getId());
        // 步骤6:遍历权限列表,将每个权限转换为Spring Security的权限对象
        for (Permission permission : permissions) {
            // 权限code直接转换为权限对象,无需添加前缀
            // 可以在安全表达式中使用hasAuthority('user:read')进行校验
            authorities.add(new SimpleGrantedAuthority(permission.getCode()));
        }

        // 步骤7:构建并返回Spring Security的UserDetails对象
        // 使用Spring Security内置的User实现类,封装认证所需的所有信息
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),      // 用户名,用于标识当前认证用户
                user.getPassword(),      // 已加密的密码(BCrypt),用于密码比对验证
                authorities              // 权限列表,包含角色权限和直接权限,用于授权决策
        );
    }
}
