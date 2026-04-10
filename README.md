# OAuth2 认证服务中心

基于 Spring Cloud + Vue3 的企业级 OAuth2 认证服务中心，支持多系统接入。

## 技术栈

| 分类 | 技术 |
|------|------|
| **后端** | Spring Boot 3.5.11, Spring Cloud 2025.0.1, Spring Security 6.4.2 |
| **前端** | Vue 3.5, TypeScript 5, Vite 6, Element Plus 2 |
| **数据库** | MySQL 8.0+, Redis |
| **认证** | OAuth2.1, JWT |

## 项目结构

```
oauth2-sso/
├── frontend/           # Vue3 前端 (端口 3000)
└── oauth-server/       # OAuth2 认证服务器 (端口 8080)
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < init.sql
```

### 2. 启动后端服务

使用 IDEA 启动以下服务：

| 服务 | 启动类 | 端口 |
|------|--------|------|
| OAuth2 服务器 | `OAuth2ServerApplication` | 8080 |

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 4. 访问系统

打开浏览器访问：**http://localhost:3000**

### 5. 登录

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | ADMIN, USER | read, write |
| user | 123456 | USER | read |

## 功能模块

- **登录认证** - JWT Token 认证，支持表单登录和 OAuth2 密码模式
- **OAuth2 授权** - 支持 OAuth2.1 标准，包括授权码模式、密码模式、客户端凭证模式等
- **客户端管理** - OAuth2 客户端配置管理（增删改查）
- **用户管理** - 基于 RBAC 的用户权限管理

## 核心特性

- ✅ 支持 OAuth2.1 标准协议
- ✅ JWT Token 颁发与验证
- ✅ 表单登录 + OAuth2 密码登录双模式
- ✅ 客户端自动注册与手动管理
- ✅ RBAC 权限模型（用户-角色-权限）
- ✅ CORS 跨域配置
- ✅ 前端单点登录集成示例

## OAuth2 端点

### 授权端点

```
GET http://localhost:8080/oauth2/authorize
?response_type=code
&client_id={client_id}
&redirect_uri={redirect_uri}
&scope=openid profile
&state={state}
```

### 认证接口（前端登录用）

```bash
POST http://localhost:8080/api/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

响应：
```json
{
  "access_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 7200
}
```

### 用户接口

```bash
GET http://localhost:8080/api/users/me
Authorization: Bearer {access_token}
```

## 环境要求

- Java 17+
- Node.js 18+
- Maven 3.6+
- MySQL 8.0+
- Redis

## 常见问题

### 1. 数据库连接失败
检查 MySQL 是否启动，用户名密码是否正确

### 2. 登录失败
- 确认数据库已执行 init.sql
- 查看控制台日志确认用户已创建
- 确认密码是 123456

### 3. 403 权限不足
JWT Token 中的 scope 未正确转换，检查服务是否正常启动

### 4. 前端无法访问后端
- 确认所有服务已启动
- 检查浏览器控制台网络请求

## 开发说明

### 数据库表结构

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_permission | 权限表 |
| sys_user_role | 用户-角色关联表 |
| sys_role_permission | 角色-权限关联表 |
| oauth2_registered_client | OAuth2 注册客户端表 |
| oauth2_authorization | OAuth2 授权表 |
| oauth2_authorization_consent | OAuth2 授权同意表 |

### 后端编译

```bash
mvn clean install -DskipTests
```

### 前端构建

```bash
cd frontend
npm run build
```

## 接入说明

本认证服务中心可为多个业务系统提供统一认证服务，业务系统只需集成 OAuth2 客户端即可接入。

详细接入说明请参考：`客户端配置说明.md`

---

**版本**: 1.0.0  
**更新时间**: 2026 年 4 月 9 日
