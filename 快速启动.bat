@echo off
chcp 65001 >nul
echo ========================================
echo   OAuth2 SSO 前后端分离系统 - 快速启动
echo ========================================
echo.

cd /d "%~dp0"

echo 正在检查环境...
echo.

REM 检查 Java
where java >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java，请安装 Java 17+
    pause
    exit /b 1
)
echo [OK] Java 已安装

REM 检查 Node.js
where node >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Node.js，请安装 Node.js 18+
    pause
    exit /b 1
)
echo [OK] Node.js 已安装

echo.
echo ========================================
echo   使用说明
echo ========================================
echo.
echo 由于 Maven 未在系统 PATH 中，请选择启动方式:
echo.
echo 方案一：使用本脚本自动启动 (需要配置 Maven)
echo   1. 设置 M2_HOME 环境变量指向 Maven 安装目录
echo   2. 或将 Maven 添加到 PATH
echo   3. 然后运行 start-all.bat
echo.
echo 方案二：使用 IDE 启动 (推荐)
echo   1. 用 IDEA 打开项目
echo   2. 分别运行以下启动类:
echo      - oauth-server/OAuth2ServerApplication (端口 8080)
echo      - gateway/GatewayApplication (端口 8081)
echo      - order-service/OrderServiceApplication (端口 8082)
echo      - user-service/UserServiceApplication (端口 8083)
echo   3. 在 frontend 目录运行：npm run dev
echo.
echo 方案三：使用命令行启动
echo   1. 确保 Maven 可用 (设置 M2_HOME 或添加到 PATH)
echo   2. 运行：start-all.bat
echo.
echo ========================================
echo.

REM 检查是否有 M2_HOME
if defined M2_HOME (
    echo [检测到 M2_HOME=%M2_HOME%]
    echo.
    set /p CONTINUE="是否使用此 Maven 继续启动？(Y/N): "
    if /i "!CONTINUE!"=="Y" (
        goto :start_services
    )
) else (
    echo [提示] 未设置 M2_HOME 环境变量
    echo.
    echo 设置方法:
    echo 1. 右键"此电脑" -> 属性 -> 高级系统设置
    echo 2. 环境变量 -> 系统变量 -> 新建
    echo 3. 变量名：M2_HOME
    echo 4. 变量值：Maven 安装路径 (如 C:\Program Files\Apache\maven)
    echo.
)

echo 按任意键退出...
pause >nul
exit /b 0

:start_services
echo.
echo ========================================
echo   启动服务
echo ========================================
echo.

set MAVEN_CMD=%M2_HOME%\bin\mvn.cmd

echo [1/4] 启动 OAuth2 认证服务器 (端口 8080)...
start "OAuth2 Server" cmd /k "cd oauth-server && !MAVEN_CMD! spring-boot:run"
timeout /t 15 /nobreak >nul

echo [2/4] 启动 API 网关 (端口 8081)...
start "API Gateway" cmd /k "cd gateway && !MAVEN_CMD! spring-boot:run"
timeout /t 10 /nobreak >nul

echo [3/4] 启动订单服务 (端口 8082)...
start "Order Service" cmd /k "cd order-service && !MAVEN_CMD! spring-boot:run"
timeout /t 10 /nobreak >nul

echo [4/4] 启动用户服务 (端口 8083)...
start "User Service" cmd /k "cd user-service && !MAVEN_CMD! spring-boot:run"
timeout /t 10 /nobreak >nul

echo.
echo [后端] 后端服务启动中...
timeout /t 10 /nobreak >nul

echo.
echo [前端] 启动前端开发服务器...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo   所有服务已启动！
echo ========================================
echo.
echo 访问地址：http://localhost:3000
echo.
echo 测试账号：admin / 123456
echo.
pause
