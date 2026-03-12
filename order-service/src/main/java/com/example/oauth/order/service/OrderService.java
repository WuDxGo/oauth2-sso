package com.example.oauth.order.service; // 定义包路径，用于组织和管理 Java 订单服务类

import com.example.oauth.order.entity.Order; // 导入订单实体类
import com.example.oauth.order.mapper.OrderMapper; // 导入订单 Mapper 接口
import lombok.RequiredArgsConstructor; // 导入 Lombok 的 RequiredArgsConstructor 注解，自动生成构造函数
import org.springframework.stereotype.Service; // 导入 Service 注解，标识此类为 Spring 服务组件
import org.springframework.transaction.annotation.Transactional; // 导入事务注解，声明事务管理

import java.time.LocalDateTime; // 导入 LocalDateTime 类，表示本地日期时间
import java.time.format.DateTimeFormatter; // 导入 DateTimeFormatter 类，格式化日期时间
import java.util.Date; // 导入 Date 类，表示日期时间
import java.util.List; // 导入 List 列表接口

/**
 * 订单服务实现类
 * 处理订单相关的业务逻辑
 */
@Service // 标识此类为 Spring 服务组件，自动注册到 Spring 容器
@RequiredArgsConstructor // Lombok 注解，生成包含所有 final 字段的构造函数
public class OrderService { // 定义订单服务类

    private final OrderMapper orderMapper; // 注入订单 Mapper 接口，用于数据库操作

    /**
     * 查询所有订单
     * @return List<Order> 订单列表
     */
    public List<Order> findAll() { // 查询所有订单的方法
        return orderMapper.findAll(); // 调用 Mapper 查询所有订单
    }

    /**
     * 根据 ID 查询订单
     * @param id 订单 ID
     * @return Order 订单对象
     */
    public Order findById(Long id) { // 根据 ID 查询订单的方法
        return orderMapper.findById(id); // 调用 Mapper 根据 ID 查询订单
    }

    /**
     * 创建订单（带事务）
     * @param order 订单对象
     * @return Order 已创建的订单对象
     */
    @Transactional // 开启事务，确保操作的原子性
    public Order create(Order order) { // 创建订单的方法
        // 生成订单号
        String orderNo = generateOrderNo(); // 调用私有方法生成唯一订单号
        order.setOrderNo(orderNo); // 设置订单号
        
        // 默认状态：待支付
        if (order.getStatus() == null) {
            order.setStatus(1); // 如果状态为空，默认为待支付（1）
        }
        
        Date now = new Date(); // 获取当前时间
        order.setCreateTime(now); // 设置创建时间为当前时间
        order.setUpdateTime(now); // 设置更新时间为当前时间
        
        orderMapper.insert(order); // 调用 Mapper 插入订单记录
        return order; // 返回已创建的订单对象
    }

    /**
     * 更新订单（带事务）
     * @param order 订单对象
     * @return Order 已更新的订单对象
     */
    @Transactional // 开启事务，确保操作的原子性
    public Order update(Order order) { // 更新订单的方法
        order.setUpdateTime(new Date()); // 设置更新时间为当前时间
        orderMapper.update(order); // 调用 Mapper 更新订单记录
        return orderMapper.findById(order.getId()); // 返回更新后的订单对象
    }

    /**
     * 删除订单（带事务）
     * @param id 订单 ID
     */
    @Transactional // 开启事务，确保操作的原子性
    public void deleteById(Long id) { // 删除订单的方法
        orderMapper.deleteById(id); // 调用 Mapper 删除订单记录
    }

    /**
     * 生成订单号（私有辅助方法）
     * 格式：yyyyMMddHHmmssSSS + 4 位随机数
     * @return String 生成的订单号
     */
    private String generateOrderNo() { // 生成订单号的私有方法
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"); // 创建日期时间格式化器，格式为年月日时分秒毫秒
        return LocalDateTime.now().format(formatter) + // 使用当前时间格式化
               (int)(Math.random() * 10000); // 添加 4 位随机数（0-9999）
    }
}
