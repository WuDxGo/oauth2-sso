package com.example.oauth.order.controller; // 定义包路径，用于组织和管理 Java 订单控制器类

import com.example.oauth.common.result.Result; // 导入统一返回结果类
import com.example.oauth.order.entity.Order; // 导入订单实体类
import com.example.oauth.order.service.OrderService; // 导入订单服务类
import lombok.RequiredArgsConstructor; // 导入 Lombok 的 RequiredArgsConstructor 注解，自动生成构造函数
import org.springframework.security.access.prepost.PreAuthorize; // 导入权限控制注解，用于方法级安全控制
import org.springframework.web.bind.annotation.*; // 导入 RESTful 注解（GetMapping、PostMapping 等）

import java.util.List; // 导入 List 列表接口

/**
 * 订单控制器
 * 处理订单相关的 HTTP 请求
 */
@RestController // 标识此类为 RESTful 控制器，返回 JSON 数据而非视图
@RequestMapping("/orders") // 设置基础请求路径为/orders
@RequiredArgsConstructor // Lombok 注解，生成包含所有 final 字段的构造函数
public class OrderController { // 定义订单控制器类

    private final OrderService orderService; // 注入订单服务实例

    /**
     * 获取所有订单
     * @return Result<List<Order>> 包含订单列表的统一响应结果
     */
    @GetMapping // 映射 GET 请求到根路径（/orders）
    @PreAuthorize("hasAuthority('read')") // 需要"read"权限才能访问此方法
    public Result<List<Order>> getAllOrders() { // 获取所有订单的方法
        List<Order> orders = orderService.findAll(); // 调用服务层查询所有订单
        return Result.success(orders); // 返回成功响应，包含订单列表
    }

    /**
     * 根据 ID 获取订单
     * @param id 订单 ID（从 URL 路径中提取）
     * @return Result<Order> 包含订单数据的统一响应结果
     */
    @GetMapping("/{id}") // 映射 GET 请求到/orders/{id}路径
    @PreAuthorize("hasAuthority('read')") // 需要"read"权限才能访问此方法
    public Result<Order> getOrderById(@PathVariable Long id) { // 根据 ID 获取订单的方法
        Order order = orderService.findById(id); // 调用服务层根据 ID 查询订单
        if (order == null) {
            return Result.error(404, "订单不存在"); // 如果订单不存在，返回 404 错误
        }
        return Result.success(order); // 返回成功响应，包含订单数据
    }

    /**
     * 创建订单
     * @param order 订单对象（从请求体中读取）
     * @return Result<Order> 包含已创建订单的统一响应结果
     */
    @PostMapping // 映射 POST 请求到根路径（/orders）
    @PreAuthorize("hasAuthority('write')") // 需要"write"权限才能访问此方法
    public Result<Order> createOrder(@RequestBody Order order) { // 创建订单的方法
        Order created = orderService.create(order); // 调用服务层创建订单
        return Result.success("创建成功", created); // 返回成功响应，包含成功消息和已创建的订单
    }

    /**
     * 更新订单
     * @param id 订单 ID（从 URL 路径中提取）
     * @param order 新的订单数据（从请求体中读取）
     * @return Result<Order> 包含已更新订单的统一响应结果
     */
    @PutMapping("/{id}") // 映射 PUT 请求到/orders/{id}路径
    @PreAuthorize("hasAuthority('write')") // 需要"write"权限才能访问此方法
    public Result<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) { // 更新订单的方法
        order.setId(id); // 设置订单 ID
        Order updated = orderService.update(order); // 调用服务层更新订单
        return Result.success("更新成功", updated); // 返回成功响应，包含成功消息和已更新的订单
    }

    /**
     * 删除订单
     * @param id 订单 ID（从 URL 路径中提取）
     * @return Result<Void> 删除操作的统一响应结果
     */
    @DeleteMapping("/{id}") // 映射 DELETE 请求到/orders/{id}路径
    @PreAuthorize("hasAuthority('write')") // 需要"write"权限才能访问此方法
    public Result<Void> deleteOrder(@PathVariable Long id) { // 删除订单的方法
        orderService.deleteById(id); // 调用服务层删除订单
        return Result.success("删除成功", null); // 返回成功响应，表示删除成功
    }
}
