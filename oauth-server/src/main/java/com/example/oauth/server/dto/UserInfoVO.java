package com.example.oauth.server.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户信息 VO
 */
@Data
public class UserInfoVO {
    /** 用户主键 ID */
    private Long id;

    /** 用户登录账号 */
    private String username;

    /** 用户显示昵称 */
    private String nickname;

    /** 用户邮箱 */
    private String email;

    /** 用户手机号 */
    private String phone;

    /** 用户头像 URL */
    private String avatar;

    /** 性别 (0:女 1:男) */
    private Integer gender;

    /** 状态 (0:禁用 1:正常) */
    private Integer status;

    /** 角色列表 */
    private List<RoleInfoVO> roles;

    /** 权限编码列表 */
    private List<String> authorities;
}
