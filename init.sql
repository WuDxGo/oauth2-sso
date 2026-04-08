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
-- OAuth2 客户端表 (Spring Authorization Server 标准表结构)
-- ===========================
-- 注意：客户端数据不再在此处硬编码，应用首次启动时会自动创建默认客户端

DROP TABLE IF EXISTS `oauth2_registered_client`;
CREATE TABLE `oauth2_registered_client` (
  `id` VARCHAR(100) NOT NULL COMMENT '客户端 ID',
  `client_id` VARCHAR(100) NOT NULL COMMENT '客户端标识',
  `client_id_issued_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '客户端 ID 颁发时间',
  `client_secret` VARCHAR(200) DEFAULT NULL COMMENT '客户端密钥',
  `client_secret_expires_at` DATETIME DEFAULT NULL COMMENT '客户端密钥过期时间',
  `client_name` VARCHAR(200) NOT NULL COMMENT '客户端名称',
  `client_authentication_methods` VARCHAR(1000) NOT NULL COMMENT '客户端认证方式',
  `authorization_grant_types` VARCHAR(1000) NOT NULL COMMENT '授权类型',
  `redirect_uris` VARCHAR(1000) DEFAULT NULL COMMENT '重定向 URI',
  `post_logout_redirect_uris` VARCHAR(1000) DEFAULT NULL COMMENT '登出后重定向 URI',
  `scopes` VARCHAR(1000) NOT NULL COMMENT '授权范围',
  `client_settings` VARCHAR(2000) NOT NULL COMMENT '客户端设置 (JSON)',
  `token_settings` VARCHAR(2000) NOT NULL COMMENT 'Token 设置 (JSON)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 注册客户端表';

DROP TABLE IF EXISTS `oauth2_authorization_consent`;
CREATE TABLE `oauth2_authorization_consent` (
  `registered_client_id` VARCHAR(100) NOT NULL COMMENT '注册客户端 ID',
  `principal_name` VARCHAR(200) NOT NULL COMMENT '主体名称',
  `authorities` VARCHAR(1000) NOT NULL COMMENT '授权列表',
  PRIMARY KEY (`registered_client_id`, `principal_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 授权同意表';

DROP TABLE IF EXISTS `oauth2_authorization`;
CREATE TABLE `oauth2_authorization` (
  `id` VARCHAR(100) NOT NULL COMMENT '授权 ID',
  `registered_client_id` VARCHAR(100) NOT NULL COMMENT '注册客户端 ID',
  `principal_name` VARCHAR(200) NOT NULL COMMENT '主体名称',
  `authorization_grant_type` VARCHAR(100) NOT NULL COMMENT '授权类型',
  `authorized_scopes` VARCHAR(1000) DEFAULT NULL COMMENT '授权范围',
  `attributes` TEXT DEFAULT NULL COMMENT '属性',
  `state` VARCHAR(500) DEFAULT NULL COMMENT '状态',
  `authorization_code_value` TEXT DEFAULT NULL COMMENT '授权码值',
  `authorization_code_issued_at` DATETIME DEFAULT NULL COMMENT '授权码颁发时间',
  `authorization_code_expires_at` DATETIME DEFAULT NULL COMMENT '授权码过期时间',
  `authorization_code_metadata` TEXT DEFAULT NULL COMMENT '授权码元数据',
  `access_token_value` TEXT DEFAULT NULL COMMENT '访问 Token 值',
  `access_token_issued_at` DATETIME DEFAULT NULL COMMENT '访问 Token 颁发时间',
  `access_token_expires_at` DATETIME DEFAULT NULL COMMENT '访问 Token 过期时间',
  `access_token_metadata` TEXT DEFAULT NULL COMMENT '访问 Token 元数据',
  `access_token_type` VARCHAR(100) DEFAULT NULL COMMENT '访问 Token 类型',
  `access_token_scopes` VARCHAR(1000) DEFAULT NULL COMMENT '访问 Token 范围',
  `oidc_id_token_value` TEXT DEFAULT NULL COMMENT 'OIDC ID Token 值',
  `oidc_id_token_issued_at` DATETIME DEFAULT NULL COMMENT 'OIDC ID Token 颁发时间',
  `oidc_id_token_expires_at` DATETIME DEFAULT NULL COMMENT 'OIDC ID Token 过期时间',
  `oidc_id_token_metadata` TEXT DEFAULT NULL COMMENT 'OIDC ID Token 元数据',
  `refresh_token_value` TEXT DEFAULT NULL COMMENT '刷新 Token 值',
  `refresh_token_issued_at` DATETIME DEFAULT NULL COMMENT '刷新 Token 颁发时间',
  `refresh_token_expires_at` DATETIME DEFAULT NULL COMMENT '刷新 Token 过期时间',
  `refresh_token_metadata` TEXT DEFAULT NULL COMMENT '刷新 Token 元数据',
  `user_code_value` TEXT DEFAULT NULL COMMENT '用户码值',
  `user_code_issued_at` DATETIME DEFAULT NULL COMMENT '用户码颁发时间',
  `user_code_expires_at` DATETIME DEFAULT NULL COMMENT '用户码过期时间',
  `user_code_metadata` TEXT DEFAULT NULL COMMENT '用户码元数据',
  `device_code_value` TEXT DEFAULT NULL COMMENT '设备码值',
  `device_code_issued_at` DATETIME DEFAULT NULL COMMENT '设备码颁发时间',
  `device_code_expires_at` DATETIME DEFAULT NULL COMMENT '设备码过期时间',
  `device_code_metadata` TEXT DEFAULT NULL COMMENT '设备码元数据',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_access_token` (`access_token_value`(255)),
  UNIQUE KEY `uk_refresh_token` (`refresh_token_value`(255)),
  UNIQUE KEY `uk_authorization_code` (`authorization_code_value`(255)),
  KEY `idx_client_id` (`registered_client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 授权表';

-- 注意：默认客户端会在应用首次启动时自动创建，无需在此处插入
