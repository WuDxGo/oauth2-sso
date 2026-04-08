package com.example.oauth.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 映射数据库中的user表,每条记录代表一个注册用户
 * 存储用户的基本信息、认证凭据和状态
 * 使用Lombok的@Data注解自动生成getter/setter/toString/equals/hashCode方法
 * 实现Serializable接口支持对象序列化和网络传输
 */
@Data
public class User implements Serializable {

    /**
     * 序列化版本UID
     * 用于反序列化时验证版本兼容性,防止类结构变化导致反序列化失败
     * serialVersionUID保持为1L表示当前为第一版本
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户主键ID
     * 数据库自增主键,唯一标识一个用户
     * 使用Long类型支持大数值(超过int范围)
     */
    private Long id;

    /**
     * 用户登录账号
     * 全局唯一,用于用户身份识别和登录
     * 可以是用户名、邮箱或手机号
     * 长度限制通常为3-50个字符
     */
    private String username;

    /**
     * 登录密码(加密存储)
     * 使用BCrypt等强哈希算法加密后存储,不存储明文
     * 格式如:{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMye...,前缀标识加密算法
     * 验证时使用PasswordEncoder.matches()方法比对
     */
    private String password;

    /**
     * 电子邮箱地址
     * 用于接收通知、找回密码等场景
     * 应符合标准邮箱格式,如user@example.com
     * 可选字段,允许为空
     */
    private String email;

    /**
     * 手机号码
     * 用于短信验证、消息通知等场景
     * 应符合手机号格式规范
     * 可选字段,允许为空
     */
    private String phone;

    /**
     * 用户头像URL地址
     * 存储用户头像图片的网络路径或本地路径
     * 可以是CDN地址、OSS地址或相对路径
     * 为空时使用默认头像
     */
    private String avatar;

    /**
     * 用户显示昵称
     * 用于界面展示,替代用户名显示
     * 可以包含中文、英文、数字等字符
     * 支持用户自定义修改
     */
    private String nickname;

    /**
     * 用户性别标识
     * 使用整数表示性别:0表示女性,1表示男性
     * 可用于个性化推荐、统计分析等场景
     * 可选字段,允许为空
     */
    private Integer gender;

    /**
     * 用户账号状态
     * 使用整数表示状态:0表示禁用(不可登录),1表示正常(可以登录)
     * 管理员可通过此字段控制用户访问权限
     * 禁用的用户无法通过认证,但数据保留不删除
     */
    private Integer status;

    /**
     * 记录创建时间
     * 使用LocalDateTime类型,不含时区信息
     * 自动记录用户注册或管理员创建的时间
     * 用于审计、统计和排序
     */
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间
     * 使用LocalDateTime类型,不含时区信息
     * 每次更新用户信息时自动更新此字段
     * 用于追踪数据变更时间和审计
     */
    private LocalDateTime updateTime;
}
