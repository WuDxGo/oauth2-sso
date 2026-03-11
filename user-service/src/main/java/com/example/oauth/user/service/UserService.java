package com.example.oauth.user.service;

import com.example.oauth.user.entity.User;
import com.example.oauth.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return userMapper.findAll();
    }

    /**
     * 根据 ID 查询用户
     */
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    /**
     * 创建用户
     */
    @Transactional
    public User create(User user) {
        // 默认状态：正常
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        
        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户
     */
    @Transactional
    public User update(User user) {
        user.setUpdateTime(new Date());
        userMapper.update(user);
        return userMapper.findById(user.getId());
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }
}
