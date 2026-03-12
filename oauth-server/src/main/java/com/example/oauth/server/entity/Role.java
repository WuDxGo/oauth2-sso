package com.example.oauth.server.entity; // 定义包路径，用于组织和管理 Java 角色实体类

import lombok.Data; // 导入 Lombok 的 Data 注解，自动生成 getter、setter、toString 等方法

import java.io.Serializable; // 导入 Serializable 接口，使对象可以被序列化，支持网络传输和持久化
import java.util.Date; // 导入 Date 类，表示日期时间

/**
 * 角色实体类
 * 对应数据库中的角色表，存储角色信息
 */
@Data // Lombok 注解，自动生成所有字段的 getter、setter、toString、equals、hashCode 方法
public class Role implements Serializable { // 定义角色实体类，实现 Serializable 接口以支持序列化

    private static final long serialVersionUID = 1L; // 序列化版本号，用于在反序列化时验证版本兼容性

    /**
     * 角色 ID 字段
     * 主键，唯一标识一个角色
     */
    private Long id; // 角色 ID 字段

    /**
     * 角色编码字段
     * 角色的唯一标识符，如"ADMIN"、"USER"
     */
    private String code; // 角色编码字段

    /**
     * 角色名称字段
     * 角色的显示名称，如"管理员"、"普通用户"
     */
    private String name; // 角色名称字段

    /**
     * 描述字段
     * 对角色的详细描述说明
     */
    private String description; // 描述字段

    /**
     * 状态字段
     * 0:禁用 1:正常
     */
    private Integer status; // 状态字段 (0:禁用 1:正常)

    /**
     * 创建时间字段
     * 记录角色创建的时间
     */
    private Date createTime; // 创建时间字段

    /**
     * 更新时间字段
     * 记录角色信息最后更新的时间
     */
    private Date updateTime; // 更新时间字段
}
