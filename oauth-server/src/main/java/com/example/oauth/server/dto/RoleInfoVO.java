package com.example.oauth.server.dto;

import lombok.Data;

/**
 * 角色信息 VO
 */
@Data
public class RoleInfoVO {
    /** 角色主键 ID */
    private Long id;

    /** 角色编码 */
    private String code;

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    private String description;
}
