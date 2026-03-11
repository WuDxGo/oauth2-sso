-- 创建数据库
CREATE DATABASE IF NOT EXISTS oauth2_sso DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE oauth2_sso;

-- ===========================
-- 用户相关表
-- ===========================

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `gender` TINYINT DEFAULT 1 COMMENT '性别 (0:女 1:男)',
  `status` TINYINT DEFAULT 1 COMMENT '状态 (0:禁用 1:正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态 (0:禁用 1:正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限 ID',
  `code` VARCHAR(50) NOT NULL COMMENT '权限编码',
  `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `type` TINYINT DEFAULT 3 COMMENT '资源类型 (1:菜单 2:按钮 3:接口)',
  `url` VARCHAR(255) DEFAULT NULL COMMENT '资源路径',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级 ID',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态 (0:禁用 1:正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `role_id` BIGINT NOT NULL COMMENT '角色 ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限 ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ===========================
-- 订单表
-- ===========================

DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单 ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `status` TINYINT DEFAULT 1 COMMENT '状态 (1:待支付 2:已支付 3:已取消)',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ===========================
-- 初始化数据
-- ===========================

-- 插入测试用户（密码：123456）
-- 使用 MySQL 的 BCRYPT 函数在线生成，确保格式正确
INSERT INTO `sys_user` (`username`, `password`, `email`, `phone`, `nickname`, `gender`, `status`) VALUES
('admin', '{bcrypt}$2a$10$sk5I31WqTxIyGoWmFo8xYuX4sR4offE3KkK1JqIH2IVLgEYxz.ika', 'admin@example.com', '13800138000', '管理员', 1, 1),
('user', '{bcrypt}$2a$10$sk5I31WqTxIyGoWmFo8xYuX4sR4offE3KkK1JqIH2IVLgEYxz.ika', 'user@example.com', '13900139000', '普通用户', 0, 1);

-- 插入角色
INSERT INTO `sys_role` (`code`, `name`, `description`, `status`) VALUES
('ADMIN', '管理员', '系统管理员，拥有所有权限', 1),
('USER', '普通用户', '普通用户，拥有基本权限', 1);

-- 插入权限
INSERT INTO `sys_permission` (`code`, `name`, `type`, `url`, `status`) VALUES
('read', '读取权限', 3, '/**/GET', 1),
('write', '写入权限', 3, '/**/POST,/**/PUT,/**/DELETE', 1);

-- 分配角色给用户
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- admin 拥有 ADMIN 角色
(1, 2), -- admin 拥有 USER 角色
(2, 2); -- user 拥有 USER 角色

-- 分配权限给角色
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), -- ADMIN 拥有 read 权限
(1, 2), -- ADMIN 拥有 write 权限
(2, 1); -- USER 拥有 read 权限

-- 插入测试订单数据
INSERT INTO `t_order` (`order_no`, `user_id`, `amount`, `status`, `description`) VALUES
('2026031200010001', 1, 100.00, 1, '测试订单 1'),
('2026031200010002', 1, 200.00, 2, '测试订单 2'),
('2026031200010003', 2, 150.00, 1, '测试订单 3');

-- 提交事务
COMMIT;
