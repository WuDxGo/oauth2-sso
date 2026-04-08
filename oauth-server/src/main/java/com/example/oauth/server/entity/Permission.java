package com.example.oauth.server.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限实体类
 * 映射数据库中的permission表,每条记录代表一个权限项
 * 权限是细粒度的访问控制单元,如"user:create"、"order:delete"
 * 与角色是多对多关系,通过role_permission中间表关联
 * 支持树形结构,通过parentId字段建立父子关系
 * 使用Lombok的@Data注解自动生成常用方法
 * 实现Serializable接口支持对象序列化
 */
@Data
public class Permission implements Serializable {

    /**
     * 序列化版本UID
     * 用于反序列化时验证版本兼容性
     * 保持为1L表示当前为第一版本
     */
    private static final long serialVersionUID = 1L;

    /**
     * 权限主键ID
     * 数据库自增主键,唯一标识一个权限
     * 使用Long类型避免数值溢出
     */
    private Long id;

    /**
     * 权限编码
     * 权限的唯一业务标识,全局不可重复
     * 使用冒号分隔的格式,如"user:create"、"order:delete"、"product:read"
     * 用于代码中的权限判断,如hasAuthority("user:create")
     * 格式通常为"资源:操作",语义清晰
     */
    private String code;

    /**
     * 权限显示名称
     * 用于界面展示和管理后台显示
     * 如"创建用户"、"删除订单"、"查看产品"
     * 支持中文,方便管理人员理解权限含义
     */
    private String name;

    /**
     * 资源类型
     * 使用整数表示权限作用的资源类型:
     * 1:菜单权限,控制用户可以看到哪些菜单
     * 2:按钮权限,控制页面中按钮的显示和可用
     * 3:接口权限,控制后端API的访问权限
     * 用于前端动态渲染菜单和按钮,后端控制接口访问
     */
    private Integer type;

    /**
     * 资源路径
     * 权限对应的URL路径或前端路由地址
     * 如"/api/users"、"/system/user"、"/admin"
     * 用于权限校验时匹配请求路径
     * 可选字段,菜单和按钮类型可能为空
     */
    private String url;

    /**
     * 父级权限ID
     * 指向父级权限的主键ID,用于构建树形权限结构
     * 为null或0表示顶级权限(一级菜单或模块)
     * 非null表示子权限(二级菜单、按钮或接口)
     * 通过递归查询可构建完整的权限树
     */
    private Long parentId;

    /**
     * 权限描述信息
     * 对权限用途和使用场景的详细说明
     * 方便管理人员了解权限作用
     * 可选字段,允许为空
     */
    private String description;

    /**
     * 权限状态
     * 使用整数表示:0表示禁用,1表示正常
     * 禁用的权限不会分配给角色,已分配的角色不受影响
     * 管理员可通过此字段控制权限的可用性
     */
    private Integer status;

    /**
     * 记录创建时间
     * 使用java.util.Date类型,包含日期和时间
     * 自动记录权限创建的时间点
     * 用于审计和追踪权限变更
     */
    private Date createTime;

    /**
     * 记录最后更新时间
     * 使用java.util.Date类型,包含日期和时间
     * 每次修改权限信息时自动更新此字段
     * 用于追踪数据变更和审计
     */
    private Date updateTime;
}
