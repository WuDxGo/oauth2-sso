package com.example.oauth.order.controller;

import com.example.oauth.common.result.Result;
import com.example.oauth.order.entity.Order;
import com.example.oauth.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取所有订单
     */
    @GetMapping
    @PreAuthorize("hasAuthority('read')")
    public Result<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return Result.success(orders);
    }

    /**
     * 根据 ID 获取订单
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('read')")
    public Result<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.findById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('write')")
    public Result<Order> createOrder(@RequestBody Order order) {
        Order created = orderService.create(order);
        return Result.success("创建成功", created);
    }

    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('write')")
    public Result<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        Order updated = orderService.update(order);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('write')")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
