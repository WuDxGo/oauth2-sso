# OAuth2 SSO 项目结构说明

## 完整目录结构

```
oauth2-sso/
├── common/                              # 通用模块
│   ├── pom.xml
│   └── src/main/java/com/example/oauth/common/
│       ├── constant/
│       │   └── CommonConstants.java    # 常量定义
│       ├── exception/
│       │   ├── BusinessException.java  # 业务异常
│       │   └── GlobalExceptionHandler.java  # 全局异常处理
│       └── result/
│           └── Result.java             # 统一返回结果
│
├── oauth-server/                        # OAuth2 认证服务器（端口 8080）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/oauth/server/
│       │   ├── OAuth2ServerApplication.java  # 启动类
│       │   ├── config/
│       │   │   ├── AuthorizationServerConfig.java  # OAuth2 服务器配置
│       │   │   └── DefaultSecurityConfig.java    # 默认安全配置
│       │   ├── controller/
│       │   │   └── LoginController.java          # 登录页面控制器
│       │   ├── entity/
│       │   │   ├── User.java                     # 用户实体
│       │   │   ├── Role.java                     # 角色实体
│       │   │   └── Permission.java               # 权限实体
│       │   ├── mapper/
│       │   │   ├── UserMapper.java               # 用户 Mapper
│       │   │   ├── RoleMapper.java               # 角色 Mapper
│       │   │   └── PermissionMapper.java         # 权限 Mapper
│       │   └── service/
│       │       └── CustomUserDetailsService.java # 用户详情服务
│       └── resources/
│           ├── application.yml                   # 配置文件
│           ├── mapper/
│           │   ├── UserMapper.xml
│           │   ├── RoleMapper.xml
│           │   └── PermissionMapper.xml
│           └── templates/
│               ├── login.html                    # 登录页面
│               └── index.html                    # 首页
│
├── gateway/                             # API 网关（端口 8081）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/oauth/gateway/
│       │   ├── GatewayApplication.java         # 启动类
│       │   └── config/
│       │       └── SecurityConfig.java         # 网关安全配置
│       └── resources/
│           └── application.yml                 # 配置文件
│
├── order-service/                       # 订单服务（端口 8082）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/oauth/order/
│       │   ├── OrderServiceApplication.java    # 启动类
│       │   ├── config/
│       │   │   └── SecurityConfig.java         # 安全配置
│       │   ├── controller/
│       │   │   └── OrderController.java        # 订单控制器
│       │   ├── entity/
│       │   │   └── Order.java                  # 订单实体
│       │   ├── mapper/
│       │   │   └── OrderMapper.java            # 订单 Mapper
│       │   └── service/
│       │       └── OrderService.java           # 订单服务
│       └── resources/
│           ├── application.yml                 # 配置文件
│           └── mapper/
│               └── OrderMapper.xml
│
├── user-service/                        # 用户服务（端口 8083）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/oauth/user/
│       │   ├── UserServiceApplication.java     # 启动类
│       │   ├── config/
│       │   │   └── SecurityConfig.java         # 安全配置
│       │   ├── controller/
│       │   │   └── UserController.java         # 用户控制器
│       │   ├── entity/
│       │   │   └── User.java                   # 用户实体
│       │   ├── mapper/
│       │   │   └── UserMapper.java             # 用户 Mapper
│       │   └── service/
│       │       └── UserService.java            # 用户服务
│       └── resources/
│           ├── application.yml                 # 配置文件
│           └── mapper/
│               └── UserMapper.xml
│
├── init.sql                             # 数据库初始化脚本
├── README.md                            # 项目说明文档
├── build.bat                            # Windows 编译脚本
├── start-all.bat                        # Windows 启动所有服务脚本
├── stop-all.bat                         # Windows 停止所有服务脚本
└── pom.xml                              # 父 POM
```

## 各模块功能说明

### 1. common（通用模块）
- 提供统一的返回结果格式
- 提供全局异常处理
- 提供常用常量定义
- 被其他所有模块依赖

### 2. oauth-server（OAuth2 认证服务器）
**端口**: 8080

**核心功能**:
- 实现 OAuth2.1 协议
- 提供授权码模式、客户端凭证模式等
- 用户认证和授权
- JWT 令牌签发和管理
- 提供登录页面

**关键配置**:
- `AuthorizationServerConfig`: OAuth2 服务器核心配置
- `DefaultSecurityConfig`: 表单登录等默认安全配置

### 3. gateway（API 网关）
**端口**: 8081

**核心功能**:
- OAuth2 客户端，处理用户登录
- 路由转发到后端服务
- JWT 令牌传递
- CORS 跨域支持

**路由规则**:
- `/api/orders/**` → `http://localhost:8082`
- `/api/users/**` → `http://localhost:8083`

### 4. order-service（订单服务）
**端口**: 8082

**核心功能**:
- 订单 CRUD 操作
- JWT 资源服务器，验证令牌
- 基于角色的权限控制

**API 接口**:
- GET `/orders` - 获取所有订单
- GET `/orders/{id}` - 获取单个订单
- POST `/orders` - 创建订单
- PUT `/orders/{id}` - 更新订单
- DELETE `/orders/{id}` - 删除订单

### 5. user-service（用户服务）
**端口**: 8083

**核心功能**:
- 用户信息 CRUD 操作
- JWT 资源服务器，验证令牌
- 基于角色的权限控制

**API 接口**:
- GET `/users` - 获取所有用户
- GET `/users/{id}` - 获取单个用户
- GET `/users/me` - 获取当前用户
- POST `/users` - 创建用户
- PUT `/users/{id}` - 更新用户
- DELETE `/users/{id}` - 删除用户

## 技术架构特点

### 1. 微服务架构
- 服务拆分清晰，职责单一
- 独立部署，互不影响
- 通过网关统一对外提供服务

### 2. 安全认证
- 基于 OAuth2.1 协议
- JWT 无状态认证
- RBAC 角色权限控制
- BCrypt 密码加密

### 3. 数据持久化
- MyBatis 持久层框架
- MySQL 关系型数据库
- Redis 缓存（可选）

### 4. 开发规范
- 统一的返回结果格式
- 完善的全局异常处理
- 详细的代码注释
- RESTful API 设计

## 数据表说明

### 系统表（sys_开头）
- `sys_user`: 用户表
- `sys_role`: 角色表
- `sys_permission`: 权限表
- `sys_user_role`: 用户角色关联表
- `sys_role_permission`: 角色权限关联表

### 业务表
- `t_order`: 订单表

## 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | ADMIN, USER | read, write |
| user | 123456 | USER | read |

## 快速上手步骤

1. 执行 `init.sql` 初始化数据库
2. 启动 Redis
3. 运行 `build.bat` 编译项目
4. 运行 `start-all.bat` 启动所有服务
5. 访问 `http://localhost:8081` 测试
6. 使用 Postman 或浏览器测试 API 接口

## 注意事项

1. 所有服务启动顺序：oauth-server → gateway → order-service → user-service
2. 确保 MySQL 和 Redis 已启动
3. 修改配置文件中的数据库连接信息
4. 生产环境请修改默认密码和密钥
