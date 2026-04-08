package com.example.oauth.server.mapper;

import com.example.oauth.server.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问Mapper接口
 * 基于MyBatis框架,通过XML映射文件执行SQL操作
 * 负责role表和user_role关联表的增删改查功能
 * 处理角色的查询以及用户与角色的关联关系
 * 使用@Mapper注解让MyBatis自动扫描并生成实现类
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据用户ID查询该用户关联的所有角色
     * 通过三表联查:user -> user_role -> role
     * 用于加载用户权限时获取用户的角色列表
     * SQL示例:SELECT r.* FROM role r INNER JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}
     *
     * @param userId 用户主键ID,指定要查询角色的用户
     * @return 角色列表,包含该用户的所有角色;如果用户没有角色则返回空列表(非null)
     */
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询角色信息
     * 角色编码是全局唯一的业务标识,如"ADMIN"、"USER"
     * 用于初始化数据时检查角色是否已存在
     * SQL示例:SELECT * FROM role WHERE code = #{code}
     *
     * @param code 角色编码,唯一标识一个角色,如"ADMIN"
     * @return 角色实体对象,包含完整的角色信息;如果角色不存在则返回null
     */
    Role findByCode(@Param("code") String code);

    /**
     * 查询数据库中的所有角色
     * 用于角色管理界面展示所有角色列表
     * SQL示例:SELECT * FROM role ORDER BY id
     *
     * @return 角色列表,包含所有角色记录;如果没有角色则返回空列表(非null)
     */
    List<Role> findAll();

    /**
     * 插入新角色记录到数据库
     * 用于创建新角色
     * 会将Role对象的所有非null字段插入到role表
     * SQL示例:INSERT INTO role(code, name, description, status) VALUES(#{role.code}, #{role.name}, ...)
     *
     * @param role 角色实体对象,包含待插入的角色信息
     * @return 受影响的行数,成功插入返回1,失败返回0
     */
    int insert(@Param("role") Role role);

    /**
     * 添加用户与角色的关联关系
     * 在user_role中间表中插入记录,建立用户和角色的多对多关系
     * 一个用户可以拥有多个角色,一个角色可以分配给多个用户
     * SQL示例:INSERT INTO user_role(user_id, role_id) VALUES(#{userId}, #{roleId})
     * 如果关联已存在会抛出唯一键冲突异常
     *
     * @param userId 用户主键ID,指定要分配角色的用户
     * @param roleId 角色主键ID,指定要分配的角色
     * @return 受影响的行数,成功添加返回1,关联已存在返回0
     */
    int addUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 添加角色与权限的关联关系
     * 在role_permission中间表中插入记录,建立角色和权限的多对多关系
     * 一个角色可以拥有多个权限,一个权限可以分配给多个角色
     * SQL示例:INSERT INTO role_permission(role_id, permission_id) VALUES(#{roleId}, #{permissionId})
     * 如果关联已存在会抛出唯一键冲突异常
     *
     * @param roleId 角色主键ID,指定要分配权限的角色
     * @param permissionId 权限主键ID,指定要分配的权限
     * @return 受影响的行数,成功添加返回1,关联已存在返回0
     */
    int addRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
