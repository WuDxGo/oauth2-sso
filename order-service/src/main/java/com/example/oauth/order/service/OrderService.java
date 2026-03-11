package com.example.oauth.order.service;

import com.example.oauth.order.entity.Order;
import com.example.oauth.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    /**
     * 查询所有订单
     */
    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    /**
     * 根据 ID 查询订单
     */
    public Order findById(Long id) {
        return orderMapper.findById(id);
    }

    /**
     * 创建订单
     */
    @Transactional
    public Order create(Order order) {
        // 生成订单号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        
        // 默认状态：待支付
        if (order.getStatus() == null) {
            order.setStatus(1);
        }
        
        Date now = new Date();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        
        orderMapper.insert(order);
        return order;
    }

    /**
     * 更新订单
     */
    @Transactional
    public Order update(Order order) {
        order.setUpdateTime(new Date());
        orderMapper.update(order);
        return orderMapper.findById(order.getId());
    }

    /**
     * 删除订单
     */
    @Transactional
    public void deleteById(Long id) {
        orderMapper.deleteById(id);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return LocalDateTime.now().format(formatter) + 
               (int)(Math.random() * 10000);
    }
}
