package com.example.oauth.order.mapper;

import com.example.oauth.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据 ID 查询订单
     */
    Order findById(@Param("id") Long id);

    /**
     * 查询所有订单
     */
    List<Order> findAll();

    /**
     * 根据用户 ID 查询订单
     */
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * 插入订单
     */
    int insert(Order order);

    /**
     * 更新订单
     */
    int update(Order order);

    /**
     * 删除订单
     */
    int deleteById(@Param("id") Long id);
}
