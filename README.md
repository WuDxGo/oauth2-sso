# OAuth2 SSO 企业级单点登录系统

## 项目简介

基于 Spring Cloud 2025.0.1、Spring Boot 3.5.11、Spring Security 6.x 的企业级 OAuth2 单点登录微服务系统。

## 技术栈

- **Spring Boot**: 3.5.11
- **Spring Cloud**: 2025.0.1
- **Spring Security**: 6.4.2
- **Spring Authorization Server**: OAuth2 认证服务器
- **MyBatis**: 持久层框架
- **MySQL**: 8.0+ 数据库
- **Redis**: 缓存
- **JWT**: Token 令牌

## 项目结构

```
oauth2-sso/
├── common/              # 通用模块（端口：无）
├── oauth-server/        # OAuth2 认证服务器（端口：8080）
├── gateway/            # API 网关（端口：8081）
├── order-service/      # 订单服务（端口：8082）
└── user-service/       # 用户服务（端口：8083）
```

## 前置要求

1. **Java 17+**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Redis**

## 快速开始

### 1. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source F:\myWork\oauth2-sso\init.sql
```

### 2. 启动 Redis

确保 Redis 在本地运行（默认端口 6379）

### 3. 编译项目

```bash
cd F:\myWork\oauth2-sso
mvn clean install -DskipTests
```

### 4. 启动服务

按以下顺序启动服务：

#### 4.1 启动 OAuth2 认证服务器（端口 8080）

```bash
cd oauth-server
mvn spring-boot:run
```

#### 4.2 启动网关（端口 8081）

```bash
cd gateway
mvn spring-boot:run
```

#### 4.3 启动订单服务（端口 8082）

```bash
cd order-service
mvn spring-boot:run
```

#### 4.4 启动用户服务（端口 8083）

```bash
cd user-service
mvn spring-boot:run
```

## 测试验证

### 1. 访问网关首页

打开浏览器访问：`http://localhost:8081`

系统会自动重定向到 OAuth2 认证服务器进行登录。

### 2. 登录信息

- **管理员账号**: `admin` / `123456`
- **普通用户**: `user` / `123456`

### 3. API 接口测试

#### 3.1 订单服务 API

通过网关访问订单服务：

```bash
# 获取所有订单
GET http://localhost:8081/api/orders/orders

# 根据 ID 获取订单
GET http://localhost:8081/api/orders/orders/1

# 创建订单
POST http://localhost:8081/api/orders/orders
Content-Type: application/json

{
  "amount": 200.00,
  "description": "测试订单"
}

# 更新订单
PUT http://localhost:8081/api/orders/orders/1
Content-Type: application/json

{
  "amount": 300.00,
  "status": 2
}

# 删除订单
DELETE http://localhost:8081/api/orders/orders/1
```

#### 3.2 用户服务 API

通过网关访问用户服务：

```bash
# 获取所有用户
GET http://localhost:8081/api/users/users

# 根据 ID 获取用户
GET http://localhost:8081/api/users/users/1

# 获取当前登录用户信息
GET http://localhost:8081/api/users/users/me

# 创建用户
POST http://localhost:8081/api/users/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000",
  "nickname": "测试用户",
  "gender": 1
}

# 更新用户
PUT http://localhost:8081/api/users/users/1
Content-Type: application/json

{
  "email": "newemail@example.com",
  "phone": "13900139000"
}

# 删除用户
DELETE http://localhost:8081/api/users/users/1
```

## OAuth2 认证流程

1. 用户访问网关 (`http://localhost:8081`)
2. 网关检测到未认证，重定向到 OAuth2 认证服务器 (`http://localhost:8080`)
3. 用户在认证服务器登录
4. 认证服务器返回授权码给网关
5. 网关使用授权码换取访问令牌（JWT）
6. 网关将 JWT 添加到请求头，转发到后端服务
7. 后端服务验证 JWT 有效性并返回数据

## 客户端配置

系统中预配置了以下 OAuth2 客户端：

### 1. gateway-client（网关客户端）

- **Client ID**: `gateway-client`
- **Client Secret**: `gateway-secret`
- **授权类型**: 授权码模式
- **重定向 URI**: `http://localhost:8081/login/oauth2/code/gateway-client`
- **权限范围**: openid, profile, read, write

### 2. order-service（订单服务）

- **Client ID**: `order-service`
- **Client Secret**: `order-secret`
- **授权类型**: 客户端凭证模式
- **权限范围**: read, write

### 3. user-service（用户服务）

- **Client ID**: `user-service`
- **Client Secret**: `user-secret`
- **授权类型**: 客户端凭证模式
- **权限范围**: read, write

## 权限控制说明

系统使用 `@PreAuthorize` 注解进行方法级别的权限控制：

- `read`: 读取权限（GET 请求）
- `write`: 写入权限（POST/PUT/DELETE 请求）

角色说明：

- `ROLE_ADMIN`: 管理员角色，拥有 read 和 write 权限
- `ROLE_USER`: 普通用户角色，仅拥有 read 权限

## 注意事项

1. **必须通过网关访问**：所有请求都应该通过网关端口 (8081)，不要直接访问后端服务
2. **JWT 令牌**：后端服务使用 JWT 进行无状态认证，令牌有效期 2 小时
3. **CSRF 保护**：示例中禁用了 CSRF 保护，生产环境需要根据实际情况配置
4. **密码加密**：所有用户密码使用 BCrypt 加密存储
5. **跨域配置**：网关已配置 CORS，支持跨域请求

## 常见问题

### 1. 数据库连接失败

检查 MySQL 是否启动，用户名密码是否正确

### 2. Redis 连接失败

检查 Redis 是否启动，默认端口是否为 6379

### 3. 认证失败

确认数据库中的客户端配置是否正确，特别是 client_secret

### 4. 权限不足

确认用户是否分配了相应的角色和权限

## 安全建议

1. 生产环境请修改默认密码
2. 启用 HTTPS 加密传输
3. 配置合适的 CORS 策略
4. 定期更新 JWT 密钥对
5. 启用审计日志记录

## 许可证

MIT License
