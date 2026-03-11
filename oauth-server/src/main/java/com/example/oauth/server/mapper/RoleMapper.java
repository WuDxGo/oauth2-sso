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
}
