package com.example.oauth.server.mapper;

import com.example.oauth.server.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限数据访问Mapper接口
 * 基于MyBatis框架,通过XML映射文件执行SQL操作
 * 负责permission表和role_permission关联表的查询功能
 * 处理权限数据的查询,用于构建用户权限树
 * 使用@Mapper注解让MyBatis自动扫描并生成实现类
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据用户ID查询该用户关联的所有权限
     * 通过多表联查:user -> user_role -> role_permission -> permission
     * 以及user -> user_permission(如果有直接分配的权限)
     * 用于加载用户权限时获取用户的完整权限列表
     * SQL示例:SELECT DISTINCT p.* FROM permission p INNER JOIN role_permission rp ON p.id = rp.permission_id INNER JOIN user_role ur ON rp.role_id = ur.role_id WHERE ur.user_id = #{userId}
     *
     * @param userId 用户主键ID,指定要查询权限的用户
     * @return 权限列表,包含该用户的所有权限(角色权限+直接权限);如果用户没有权限则返回空列表(非null)
     */
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询该角色关联的所有权限
     * 通过两表联查:role -> role_permission -> permission
     * 用于查看角色拥有的权限,或为角色分配权限时检查
     * SQL示例:SELECT p.* FROM permission p INNER JOIN role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId}
     *
     * @param roleId 角色主键ID,指定要查询权限的角色
     * @return 权限列表,包含该角色的所有权限;如果角色没有权限则返回空列表(非null)
     */
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限编码查询权限信息
     * 权限编码是全局唯一的业务标识,如"user:create"、"order:delete"
     * 用于初始化数据时检查权限是否已存在
     * SQL示例:SELECT * FROM permission WHERE code = #{code}
     *
     * @param code 权限编码,唯一标识一个权限,如"user:create"
     * @return 权限实体对象,包含完整的权限信息;如果权限不存在则返回null
     */
    Permission findByCode(@Param("code") String code);

    /**
     * 插入新权限记录到数据库
     * 用于创建新权限,如新增某个操作的访问权限
     * 会将Permission对象的所有非null字段插入到permission表
     * SQL示例:INSERT INTO permission(code, name, type, url, parent_id, description, status) VALUES(#{permission.code}, ...)
     *
     * @param permission 权限实体对象,包含待插入的权限信息
     * @return 受影响的行数,成功插入返回1,失败返回0
     */
    int insert(@Param("permission") Permission permission);
}
