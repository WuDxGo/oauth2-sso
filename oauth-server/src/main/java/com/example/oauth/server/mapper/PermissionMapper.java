package com.example.oauth.server.mapper;

import com.example.oauth.server.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper 接口
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据用户 ID 查询权限列表
     *
     * @param userId 用户 ID
     * @return 权限列表
     */
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色 ID 查询权限列表
     *
     * @param roleId 角色 ID
     * @return 权限列表
     */
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限编码查询权限
     *
     * @param code 权限编码
     * @return 权限信息
     */
    Permission findByCode(@Param("code") String code);

    /**
     * 插入权限
     *
     * @param permission 权限信息
     * @return 影响行数
     */
    int insert(@Param("permission") Permission permission);
}
