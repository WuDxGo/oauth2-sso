package com.example.oauth.server.service;

import com.example.oauth.server.dto.RoleInfoVO;
import com.example.oauth.server.dto.UserInfoVO;
import com.example.oauth.server.entity.Permission;
import com.example.oauth.server.entity.Role;
import com.example.oauth.server.entity.User;
import com.example.oauth.server.mapper.PermissionMapper;
import com.example.oauth.server.mapper.RoleMapper;
import com.example.oauth.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息服务类
 * 负责用户信息的查询和转换
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 获取当前用户信息
     * 如果数据库查询失败，返回基于认证信息的临时用户信息
     *
     * @param authentication 认证对象
     * @return 用户信息 VO
     */
    public UserInfoVO getCurrentUser(Authentication authentication) {
        String username = authentication.getName();

        try {
            User user = userMapper.findByUsername(username);
            if (user == null) {
                log.warn("用户 {} 在数据库中不存在，返回临时用户信息", username);
                return buildTemporaryUserInfo(username, authentication);
            }

            return buildUserInfoFromDatabase(user);
        } catch (Exception e) {
            log.error("获取用户 {} 信息失败，返回临时用户信息: {}", username, e.getMessage());
            return buildTemporaryUserInfo(username, authentication);
        }
    }

    /**
     * 从数据库构建用户信息
     */
    private UserInfoVO buildUserInfoFromDatabase(User user) {
        List<RoleInfoVO> roleInfos = roleMapper.findByUserId(user.getId()).stream()
            .map(this::convertToRoleInfoVO)
            .collect(Collectors.toList());

        List<String> permissionCodes = permissionMapper.findByUserId(user.getId()).stream()
            .map(Permission::getCode)
            .collect(Collectors.toList());

        UserInfoVO info = new UserInfoVO();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());
        info.setAvatar(user.getAvatar());
        info.setGender(user.getGender());
        info.setStatus(user.getStatus());
        info.setRoles(roleInfos);
        info.setAuthorities(permissionCodes);

        return info;
    }

    /**
     * 构建临时用户信息
     */
    private UserInfoVO buildTemporaryUserInfo(String username, Authentication authentication) {
        UserInfoVO info = new UserInfoVO();
        info.setId(-1L);
        info.setUsername(username);
        info.setNickname(username);
        info.setEmail(username + "@example.com");
        info.setGender(1);
        info.setStatus(1);

        List<RoleInfoVO> tempRoles = authentication.getAuthorities().stream()
            .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
            .map(auth -> {
                RoleInfoVO roleInfo = new RoleInfoVO();
                roleInfo.setId(-1L);
                String roleCode = auth.getAuthority().substring(5);
                roleInfo.setCode(roleCode);
                roleInfo.setName(roleCode);
                roleInfo.setDescription("临时角色");
                return roleInfo;
            })
            .collect(Collectors.toList());

        List<String> tempAuthorities = authentication.getAuthorities().stream()
            .filter(auth -> !auth.getAuthority().startsWith("ROLE_"))
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        info.setRoles(tempRoles.isEmpty() ? null : tempRoles);
        info.setAuthorities(tempAuthorities.isEmpty() ? List.of("read", "write") : tempAuthorities);

        return info;
    }

    /**
     * 转换角色实体为 VO
     */
    private RoleInfoVO convertToRoleInfoVO(Role role) {
        RoleInfoVO vo = new RoleInfoVO();
        vo.setId(role.getId());
        vo.setCode(role.getCode());
        vo.setName(role.getName());
        vo.setDescription(role.getDescription());
        return vo;
    }
}
