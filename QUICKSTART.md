# 快速开始指南

## 5 分钟快速启动 OAuth2 SSO 系统

### 前置条件检查

确保以下软件已安装并运行：

1. ✅ Java 17+
2. ✅ Maven 3.6+
3. ✅ MySQL 8.0+
4. ✅ Redis

### 步骤 1：初始化数据库（1 分钟）

打开命令行，登录 MySQL 并执行初始化脚本：

```bash
mysql -u root -p
```

在 MySQL 命令行中执行：

```sql
source F:\myWork\oauth2-sso\init.sql
```

看到 "Query OK" 提示表示成功。

### 步骤 2：确认 Redis 已启动（30 秒）

检查 Redis 是否在运行：

```bash
redis-cli ping
```

如果返回 `PONG`，说明 Redis 正常运行。

### 步骤 3：编译项目（2-3 分钟）

在项目根目录执行：

```bash
cd F:\myWork\oauth2-sso
build.bat
```

等待编译完成，看到 "√ 编译成功！" 提示。

### 步骤 4：启动所有服务（1 分钟）

运行启动脚本：

```bash
start-all.bat
```

脚本会自动按顺序启动所有 4 个服务：
1. OAuth2 认证服务器（端口 8080）
2. API 网关（端口 8081）
3. 订单服务（端口 8082）
4. 用户服务（端口 8083）

### 步骤 5：测试验证（1 分钟）

#### 5.1 访问网关首页

打开浏览器访问：http://localhost:8081

系统会自动跳转到登录页面 http://localhost:8080/login

#### 5.2 使用测试账号登录

- 管理员账号：**admin** / **123456**
- 普通用户：**user** / **123456**

#### 5.3 测试 API 接口

使用 Postman 或浏览器测试（需要先通过网关登录获取 JWT Token）：

**获取订单列表：**
```
GET http://localhost:8081/api/orders/orders
```

**获取用户列表：**
```
GET http://localhost:8081/api/users/users
```

**获取当前用户信息：**
```
GET http://localhost:8081/api/users/users/me
```

### 成功标志

如果您能看到登录页面并成功登录，说明系统已正常运行！🎉

---

## 常见问题排查

### 问题 1：数据库连接失败

**错误信息**: `Communications link failure`

**解决方案**:
1. 检查 MySQL 服务是否启动
2. 确认用户名密码正确
3. 检查数据库 `oauth2_sso` 是否已创建

### 问题 2：Redis 连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
1. 启动 Redis 服务
2. 检查 Redis 是否在默认端口 6379 运行

### 问题 3：端口被占用

**错误信息**: `Port 8080 is already in use`

**解决方案**:
1. 修改对应服务的 `application.yml` 中的端口号
2. 或者停止占用端口的其他应用

### 问题 4：认证失败

**错误信息**: `Invalid client credentials`

**解决方案**:
1. 确认数据库中客户端配置存在
2. 检查 `AuthorizationServerConfig.java` 中的客户端配置

---

## 下一步

系统启动成功后，您可以：

1. 📖 阅读 [README.md](README.md) 了解详细功能
2. 🔍 查看 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) 了解项目结构
3. 🛠️ 根据需求修改代码和配置
4. 🚀 部署到生产环境

---

## 停止服务

需要停止所有服务时，运行：

```bash
stop-all.bat
```

这会停止所有 Java 进程。
