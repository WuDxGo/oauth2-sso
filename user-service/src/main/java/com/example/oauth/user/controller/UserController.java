package com.example.oauth.user.controller; // 定义包路径，用于组织和管理 Java 用户控制器类

import com.example.oauth.common.controller.BaseController; // 导入基础控制器类
import com.example.oauth.common.result.Result; // 导入统一返回结果类
import com.example.oauth.user.entity.User; // 导入用户实体类
import com.example.oauth.user.service.UserService; // 导入用户服务类
import lombok.RequiredArgsConstructor; // 导入 Lombok 的 RequiredArgsConstructor 注解，自动生成构造函数
import org.springframework.security.access.prepost.PreAuthorize; // 导入权限控制注解，用于方法级安全控制
import org.springframework.web.bind.annotation.*; // 导入 RESTful 注解（GetMapping、PostMapping 等）

import java.util.List; // 导入 List 列表接口

/**
 * 用户控制器
 * 处理用户相关的 HTTP 请求
 */
@RestController // 标识此类为 RESTful 控制器，返回 JSON 数据而非视图
@RequestMapping("/users") // 设置基础请求路径为/users
@RequiredArgsConstructor // Lombok 注解，生成包含所有 final 字段的构造函数
public class UserController extends BaseController { // 定义用户控制器类，继承基础控制器

    private final UserService userService; // 注入用户服务实例

    /**
     * 获取所有用户
     * @return Result<List<User>> 包含用户列表的统一响应结果
     */
    @GetMapping // 映射 GET 请求到根路径（/users）
    @PreAuthorize("hasAuthority('read')") // 需要"read"权限才能访问此方法
    public Result<List<User>> getAllUsers() { // 获取所有用户的方法
        List<User> users = userService.findAll(); // 调用服务层查询所有用户
        return Result.success(users); // 返回成功响应，包含用户列表
    }

    /**
     * 根据 ID 获取用户
     * @param id 用户 ID（从 URL 路径中提取）
     * @return Result<User> 包含用户数据的统一响应结果
     */
    @GetMapping("/{id}") // 映射 GET 请求到/users/{id}路径
    @PreAuthorize("hasAuthority('read')") // 需要"read"权限才能访问此方法
    public Result<User> getUserById(@PathVariable Long id) { // 根据 ID 获取用户的方法
        User user = userService.findById(id); // 调用服务层根据 ID 查询用户
        return handleNotNull(user, "用户不存在"); // 使用基类方法处理 null 判断并返回响应
    }

    /**
     * 获取当前登录用户信息
     * @return Result<User> 包含当前用户信息的统一响应结果
     */
    @GetMapping("/me") // 映射 GET 请求到/users/me 路径
    @PreAuthorize("hasAuthority('read')") // 需要"read"权限才能访问此方法
    public Result<User> getCurrentUser() { // 获取当前用户的方法
        // 从 JWT Token 中获取用户信息
        String username = getCurrentUsername(); // 使用基类方法获取当前登录用户名
        User user = userService.findByUsername(username); // 调用服务层根据用户名查询用户
        return handleNotNull(user, "用户不存在"); // 使用基类方法处理 null 判断并返回响应
    }

    /**
     * 创建用户
     * @param user 用户对象（从请求体中读取）
     * @return Result<User> 包含已创建用户的统一响应结果
     */
    @PostMapping // 映射 POST 请求到根路径（/users）
    @PreAuthorize("hasAuthority('write')") // 需要"write"权限才能访问此方法
    public Result<User> createUser(@RequestBody User user) { // 创建用户的方法
        return handleSuccess(() -> userService.create(user), "创建成功"); // 使用基类方法处理业务逻辑并返回响应
    }

    /**
     * 更新用户
     * @param id 用户 ID（从 URL 路径中提取）
     * @param user 新的用户数据（从请求体中读取）
     * @return Result<User> 包含已更新用户的统一响应结果
     */
    @PutMapping("/{id}") // 映射 PUT 请求到/users/{id}路径
    @PreAuthorize("hasAuthority('write')") // 需要"write"权限才能访问此方法
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) { // 更新用户的方法
        user.setId(id); // 设置用户 ID
        return handleSuccess(() -> userService.update(user), "更新成功"); // 使用基类方法处理业务逻辑并返回响应
    }

    /**
     * 删除用户
     * @param id 用户 ID（从 URL 路径中提取）
     * @return Result<Void> 删除操作的统一响应结果
     */
    @DeleteMapping("/{id}") // 映射 DELETE 请求到/users/{id}路径
    @PreAuthorize("hasAuthority('write')") // 需要"write"权限才能访问此方法
    public Result<Void> deleteUser(@PathVariable Long id) { // 删除用户的方法
        userService.deleteById(id); // 调用服务层删除用户
        return Result.success("删除成功", null); // 返回成功响应，表示删除成功
    }
}
