package com.example.oauth.user.mapper; // 定义包路径，用于组织和管理 Java 用户 Mapper 接口

import com.example.oauth.user.entity.User; // 导入用户实体类
import org.apache.ibatis.annotations.Mapper; // 导入 MyBatis 的 Mapper 注解，标识此为 Mapper 接口
import org.apache.ibatis.annotations.Param; // 导入 MyBatis 的 Param 注解，为参数命名

import java.util.List; // 导入 List 列表接口

/**
 * 用户 Mapper 接口
 * 使用 MyBatis 框架操作数据库中的用户表
 */
@Mapper // MyBatis 注解，标识此接口为 Mapper Bean，自动注册到 Spring 容器
public interface UserMapper { // 定义用户 Mapper 接口

    /**
     * 根据 ID 查询用户
     * @param id 用户 ID
     * @return User 用户对象
     */
    User findById(@Param("id") Long id); // 根据 ID 查询用户的方法

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return User 用户对象
     */
    User findByUsername(@Param("username") String username); // 根据用户名查询用户的方法

    /**
     * 查询所有用户
     * @return List<User> 用户列表
     */
    List<User> findAll(); // 查询所有用户的方法

    /**
     * 插入用户记录
     * @param user 用户对象
     * @return int 影响的行数（通常为 1）
     */
    int insert(User user); // 插入用户的方法

    /**
     * 更新用户记录
     * @param user 用户对象
     * @return int 影响的行数（通常为 1）
     */
    int update(User user); // 更新用户的方法

    /**
     * 根据 ID 删除用户记录
     * @param id 用户 ID
     * @return int 影响的行数（通常为 1）
     */
    int deleteById(@Param("id") Long id); // 删除用户的方法
}
