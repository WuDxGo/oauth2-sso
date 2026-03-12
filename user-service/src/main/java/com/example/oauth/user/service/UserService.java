package com.example.oauth.user.service; // 定义包路径，用于组织和管理 Java 用户服务类

import com.example.oauth.user.entity.User; // 导入用户实体类
import com.example.oauth.user.mapper.UserMapper; // 导入用户 Mapper 接口
import lombok.RequiredArgsConstructor; // 导入 Lombok 的 RequiredArgsConstructor 注解，自动生成构造函数
import org.springframework.stereotype.Service; // 导入 Service 注解，标识此类为 Spring 服务组件
import org.springframework.transaction.annotation.Transactional; // 导入事务注解，声明事务管理

import java.time.LocalDateTime; // 导入 LocalDateTime 类，表示本地日期时间
import java.util.Date; // 导入 Date 类，表示日期时间
import java.util.List; // 导入 List 列表接口

/**
 * 用户服务实现类
 * 处理用户相关的业务逻辑
 */
@Service // 标识此类为 Spring 服务组件，自动注册到 Spring 容器
@RequiredArgsConstructor // Lombok 注解，生成包含所有 final 字段的构造函数
public class UserService { // 定义用户服务类

    private final UserMapper userMapper; // 注入用户 Mapper 接口，用于数据库操作

    /**
     * 查询所有用户
     * @return List<User> 用户列表
     */
    public List<User> findAll() { // 查询所有用户的方法
        return userMapper.findAll(); // 调用 Mapper 查询所有用户
    }

    /**
     * 根据 ID 查询用户
     * @param id 用户 ID
     * @return User 用户对象
     */
    public User findById(Long id) { // 根据 ID 查询用户的方法
        return userMapper.findById(id); // 调用 Mapper 根据 ID 查询用户
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return User 用户对象
     */
    public User findByUsername(String username) { // 根据用户名查询用户的方法
        return userMapper.findByUsername(username); // 调用 Mapper 根据用户名查询用户
    }

    /**
     * 创建用户（带事务）
     * @param user 用户对象
     * @return User 已创建的用户对象
     */
    @Transactional // 开启事务，确保操作的原子性
    public User create(User user) { // 创建用户的方法
        // 默认状态：正常
        if (user.getStatus() == null) {
            user.setStatus(1); // 如果状态为空，默认为正常（1）
        }
        
        Date now = new Date(); // 获取当前时间
        user.setCreateTime(now); // 设置创建时间为当前时间
        user.setUpdateTime(now); // 设置更新时间为当前时间
        
        userMapper.insert(user); // 调用 Mapper 插入用户记录
        return user; // 返回已创建的用户对象
    }

    /**
     * 更新用户（带事务）
     * @param user 用户对象
     * @return User 已更新的用户对象
     */
    @Transactional // 开启事务，确保操作的原子性
    public User update(User user) { // 更新用户的方法
        user.setUpdateTime(new Date()); // 设置更新时间为当前时间
        userMapper.update(user); // 调用 Mapper 更新用户记录
        return userMapper.findById(user.getId()); // 返回更新后的用户对象
    }

    /**
     * 删除用户（带事务）
     * @param id 用户 ID
     */
    @Transactional // 开启事务，确保操作的原子性
    public void deleteById(Long id) { // 删除用户的方法
        userMapper.deleteById(id); // 调用 Mapper 删除用户记录
    }
}
