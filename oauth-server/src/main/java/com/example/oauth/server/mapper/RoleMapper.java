package com.example.oauth.server.mapper;

import com.example.oauth.server.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper 接口
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据用户 ID 查询角色列表
     *
     * @param userId 用户 ID
     * @return 角色列表
     */
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询角色
     *
     * @param code 角色编码
     * @return 角色信息
     */
    Role findByCode(@Param("code") String code);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> findAll();

    /**
     * 插入角色
     *
     * @param role 角色信息
     * @return 影响行数
     */
    int insert(@Param("role") Role role);

    /**
     * 添加用户角色关联
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @return 影响行数
     */
    int addUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 添加角色权限关联
     *
     * @param roleId 角色 ID
     * @param permissionId 权限 ID
     * @return 影响行数
     */
    int addRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
