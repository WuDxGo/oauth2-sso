package com.example.oauth.server.entity; // 定义包路径，用于组织和管理 Java 权限实体类

import lombok.Data; // 导入 Lombok 的 Data 注解，自动生成 getter、setter、toString 等方法

import java.io.Serializable; // 导入 Serializable 接口，使对象可以被序列化，支持网络传输和持久化
import java.util.Date; // 导入 Date 类，表示日期时间

/**
 * 权限实体类
 * 对应数据库中的权限表，存储权限信息
 */
@Data // Lombok 注解，自动生成所有字段的 getter、setter、toString、equals、hashCode 方法
public class Permission implements Serializable { // 定义权限实体类，实现 Serializable 接口以支持序列化

    private static final long serialVersionUID = 1L; // 序列化版本号，用于在反序列化时验证版本兼容性

    /**
     * 权限 ID 字段
     * 主键，唯一标识一个权限
     */
    private Long id; // 权限 ID 字段

    /**
     * 权限编码字段
     * 权限的唯一标识符，如"user:create"、"order:delete"
     */
    private String code; // 权限编码字段

    /**
     * 权限名称字段
     * 权限的显示名称，如"创建用户"、"删除订单"
     */
    private String name; // 权限名称字段

    /**
     * 资源类型字段
     * 1:菜单 2:按钮 3:接口
     */
    private Integer type; // 资源类型字段 (1:菜单 2:按钮 3:接口)

    /**
     * 资源路径字段
     * 权限对应的 URL 路径或资源地址
     */
    private String url; // 资源路径字段

    /**
     * 父级 ID 字段
     * 指向父级权限的 ID，用于构建权限树形结构
     */
    private Long parentId; // 父级 ID 字段

    /**
     * 描述字段
     * 对权限的详细描述说明
     */
    private String description; // 描述字段

    /**
     * 状态字段
     * 0:禁用 1:正常
     */
    private Integer status; // 状态字段 (0:禁用 1:正常)

    /**
     * 创建时间字段
     * 记录权限创建的时间
     */
    private Date createTime; // 创建时间字段

    /**
     * 更新时间字段
     * 记录权限信息最后更新的时间
     */
    private Date updateTime; // 更新时间字段
}
