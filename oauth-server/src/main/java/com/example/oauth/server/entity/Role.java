package com.example.oauth.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色实体类
 * 映射数据库中的role表,每条记录代表一个角色
 * 角色是一组权限的集合,用于简化用户权限管理
 * 用户与角色是多对多关系,通过user_role中间表关联
 * 使用Lombok的@Data注解自动生成常用方法
 * 实现Serializable接口支持对象序列化
 */
@Data
public class Role implements Serializable {

    /**
     * 序列化版本UID
     * 用于反序列化时验证版本兼容性
     * 保持为1L表示当前为第一版本,类结构变化时应更新
     */
    private static final long serialVersionUID = 1L;

    /**
     * 角色主键ID
     * 数据库自增主键,唯一标识一个角色
     * 使用Long类型避免数值溢出
     */
    private Long id;

    /**
     * 角色编码
     * 角色的唯一业务标识,全局不可重复
     * 使用大写英文和下划线,如"ADMIN"、"USER"、"EDITOR"
     * 用于代码中的权限判断,如hasRole("ADMIN")
     * Spring Security会在前面自动添加"ROLE_"前缀
     */
    private String code;

    /**
     * 角色显示名称
     * 用于界面展示和管理后台显示
     * 如"管理员"、"普通用户"、"编辑人员"
     * 支持中文,方便管理人员识别
     */
    private String name;

    /**
     * 角色描述信息
     * 对角色用途和权限范围的详细说明
     * 方便管理人员了解角色职责
     * 可选字段,允许为空
     */
    private String description;

    /**
     * 角色状态
     * 使用整数表示:0表示禁用,1表示正常
     * 禁用的角色不会分配给用户,已分配的用户权限不受影响
     * 管理员可通过此字段控制角色的可用性
     */
    private Integer status;

    /**
     * 记录创建时间
     * 使用java.util.Date类型,包含日期和时间
     * 自动记录角色创建的时间点
     * 用于审计和追踪角色变更
     */
    private Date createTime;

    /**
     * 记录最后更新时间
     * 使用java.util.Date类型,包含日期和时间
     * 每次修改角色信息时自动更新此字段
     * 用于追踪数据变更和审计
     */
    private Date updateTime;
}
