package com.example.oauth.server.mapper;

import com.example.oauth.server.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问Mapper接口
 * 基于MyBatis框架,通过XML映射文件或注解执行SQL操作
 * 负责user表的增删改查(CRUD)功能
 * 使用@Mapper注解让MyBatis自动扫描并生成实现类
 * 接口方法名应与XML映射文件中的statement ID保持一致
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户信息
     * 用于用户登录认证时加载用户详情
     * SQL示例:SELECT * FROM user WHERE username = #{username}
     *
     * @param username 用户登录账号,唯一标识
     * @return 用户实体对象,包含完整的用户信息;如果用户不存在则返回null
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户信息
     * 用于根据主键获取用户详情
     * SQL示例:SELECT * FROM user WHERE id = #{id}
     *
     * @param id 用户主键ID,数据库自增主键
     * @return 用户实体对象,包含完整的用户信息;如果用户不存在则返回null
     */
    User findById(@Param("id") Long id);

    /**
     * 插入新用户记录到数据库
     * 用于用户注册或管理员创建用户
     * 会将User对象的所有非null字段插入到user表
     * SQL示例:INSERT INTO user(username, password, email, ...) VALUES(#{username}, #{password}, ...)
     * 插入后会自动回填自增主键ID到User对象的id字段
     *
     * @param user 用户实体对象,包含待插入的用户信息
     * @return 受影响的行数,成功插入返回1,失败返回0
     */
    int insert(User user);

    /**
     * 更新已存在的用户记录
     * 用于修改用户信息,如昵称、邮箱、手机号等
     * 只会更新User对象中非null的字段
     * SQL示例:UPDATE user SET nickname = #{nickname}, email = #{email} WHERE id = #{id}
     *
     * @param user 用户实体对象,必须包含有效的id字段和需要更新的字段
     * @return 受影响的行数,成功更新返回1,未找到用户返回0
     */
    int update(User user);

    /**
     * 根据用户ID删除用户记录
     * 用于注销或删除用户
     * SQL示例:DELETE FROM user WHERE id = #{id}
     * 注意:这是物理删除,数据无法恢复
     * 建议使用逻辑删除(更新status字段)替代物理删除
     *
     * @param id 用户主键ID,指定要删除的用户
     * @return 受影响的行数,成功删除返回1,未找到用户返回0
     */
    int deleteById(@Param("id") Long id);
}
