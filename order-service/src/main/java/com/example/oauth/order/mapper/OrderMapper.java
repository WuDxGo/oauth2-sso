package com.example.oauth.order.mapper; // 定义包路径，用于组织和管理 Java 订单 Mapper 接口

import com.example.oauth.order.entity.Order; // 导入订单实体类
import org.apache.ibatis.annotations.Mapper; // 导入 MyBatis 的 Mapper 注解，标识此为 Mapper 接口
import org.apache.ibatis.annotations.Param; // 导入 MyBatis 的 Param 注解，为参数命名

import java.util.List; // 导入 List 列表接口

/**
 * 订单 Mapper 接口
 * 使用 MyBatis 框架操作数据库中的订单表
 */
@Mapper // MyBatis 注解，标识此接口为 Mapper Bean，自动注册到 Spring 容器
public interface OrderMapper { // 定义订单 Mapper 接口

    /**
     * 根据 ID 查询订单
     * @param id 订单 ID
     * @return Order 订单对象
     */
    Order findById(@Param("id") Long id); // 根据 ID 查询订单的方法

    /**
     * 查询所有订单
     * @return List<Order> 订单列表
     */
    List<Order> findAll(); // 查询所有订单的方法

    /**
     * 根据用户 ID 查询订单
     * @param userId 用户 ID
     * @return List<Order> 订单列表
     */
    List<Order> findByUserId(@Param("userId") Long userId); // 根据用户 ID 查询订单的方法

    /**
     * 插入订单记录
     * @param order 订单对象
     * @return int 影响的行数（通常为 1）
     */
    int insert(Order order); // 插入订单的方法

    /**
     * 更新订单记录
     * @param order 订单对象
     * @return int 影响的行数（通常为 1）
     */
    int update(Order order); // 更新订单的方法

    /**
     * 根据 ID 删除订单记录
     * @param id 订单 ID
     * @return int 影响的行数（通常为 1）
     */
    int deleteById(@Param("id") Long id); // 删除订单的方法
}
