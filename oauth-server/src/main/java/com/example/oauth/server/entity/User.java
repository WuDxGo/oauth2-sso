package com.example.oauth.server.entity; // 定义包路径，用于组织和管理 Java 用户实体类

import lombok.Data; // 导入 Lombok 的 Data 注解，自动生成 getter、setter、toString 等方法

import java.io.Serializable; // 导入 Serializable 接口，使对象可以被序列化，支持网络传输和持久化
import java.util.Date; // 导入 Date 类，表示日期时间

/**
 * 用户实体类
 * 对应数据库中的用户表，存储用户基本信息
 */
@Data // Lombok 注解，自动生成所有字段的 getter、setter、toString、equals、hashCode 方法
public class User implements Serializable { // 定义用户实体类，实现 Serializable 接口以支持序列化

    private static final long serialVersionUID = 1L; // 序列化版本号，用于在反序列化时验证版本兼容性

    /**
     * 用户 ID 字段
     * 主键，唯一标识一个用户
     */
    private Long id; // 用户 ID 字段

    /**
     * 用户名字段
     * 用于登录和展示的唯一标识符
     */
    private String username; // 用户名字段

    /**
     * 密码字段
     * 加密存储的用户密码
     */
    private String password; // 密码字段（加密存储）

    /**
     * 邮箱字段
     * 用户的电子邮件地址
     */
    private String email; // 邮箱字段

    /**
     * 手机号字段
     * 用户的移动电话号码
     */
    private String phone; // 手机号字段

    /**
     * 头像 URL 字段
     * 用户头像图片的网络地址
     */
    private String avatar; // 头像 URL 字段

    /**
     * 昵称为字段
     * 用户自定义的显示名称
     */
    private String nickname; // 昵称字段

    /**
     * 性别字段
     * 0:女 1:男
     */
    private Integer gender; // 性别字段 (0:女 1:男)

    /**
     * 状态字段
     * 0:禁用 1:正常
     */
    private Integer status; // 状态字段 (0:禁用 1:正常)

    /**
     * 创建时间字段
     * 记录用户创建的时间
     */
    private Date createTime; // 创建时间字段

    /**
     * 更新时间字段
     * 记录用户信息最后更新的时间
     */
    private Date updateTime; // 更新时间字段
}
