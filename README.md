# OAuth2 SSO 前后端分离系统

基于 Spring Cloud + Vue3 的企业级 OAuth2 单点登录系统。

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
├── oauth-server/       # OAuth2 认证服务器 (端口 8080)
├── gateway/           # API 网关 (端口 8081)
├── order-service/     # 订单服务 (端口 8082)
├── user-service/      # 用户服务 (端口 8083)
└── common/            # 通用模块
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < init.sql
```

### 2. 启动后端服务

使用 IDEA 依次启动以下服务：

| 服务 | 启动类 | 端口 |
|------|--------|------|
| OAuth2 服务器 | `OAuth2ServerApplication` | 8080 |
| API 网关 | `GatewayApplication` | 8081 |
| 订单服务 | `OrderServiceApplication` | 8082 |
| 用户服务 | `UserServiceApplication` | 8083 |

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

- **登录认证** - JWT Token 认证，有效期 2 小时
- **首页仪表盘** - 数据统计展示
- **用户管理** - 用户 CRUD 操作
- **订单管理** - 订单 CRUD 操作

## API 接口

### 认证接口

```bash
POST http://localhost:8081/api/login
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
GET http://localhost:8081/api/users/me
Authorization: Bearer {access_token}
```

### 订单接口

```bash
GET http://localhost:8081/api/orders
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

### 后端编译

```bash
mvn clean install -DskipTests
```

### 前端构建

```bash
cd frontend
npm run build
```

## 脚本文件

| 脚本 | 说明 |
|------|------|
| `build.bat` | 编译前后端项目 |
| `start-all.bat` | 启动所有服务 |
| `stop-all.bat` | 停止所有服务 |

---

**版本**: 1.0.0  
**更新时间**: 2026 年 3 月 21 日
