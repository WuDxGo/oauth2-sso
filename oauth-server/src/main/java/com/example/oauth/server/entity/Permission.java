package com.example.oauth.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限实体类
 */
@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限 ID
     */
    private Long id;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 资源类型 (1:菜单 2:按钮 3:接口)
     */
    private Integer type;

    /**
     * 资源路径
     */
    private String url;

    /**
     * 父级 ID
     */
    private Long parentId;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态 (0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
