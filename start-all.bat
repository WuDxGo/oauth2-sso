@echo off
chcp 65001 >nul
echo ========================================
echo    OAuth2 SSO 系统 - 启动所有服务
echo ========================================
echo.

cd /d "%~dp0"

echo 正在启动所有服务...
echo.

REM 设置日志目录
set LOG_DIR=%~dp0logs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo [1/4] 启动 OAuth2 认证服务器（端口 8080）...
start "OAuth2 Server" cmd /k "cd oauth-server && call mvn spring-boot:run > ../logs/oauth-server.log 2>&1"
timeout /t 3 /nobreak >nul

echo [2/4] 启动网关服务（端口 8081）...
start "Gateway" cmd /k "cd gateway && call mvn spring-boot:run > ../logs/gateway.log 2>&1"
timeout /t 3 /nobreak >nul

echo [3/4] 启动订单服务（端口 8082）...
start "Order Service" cmd /k "cd order-service && call mvn spring-boot:run > ../logs/order-service.log 2>&1"
timeout /t 3 /nobreak >nul

echo [4/4] 启动用户服务（端口 8083）...
start "User Service" cmd /k "cd user-service && call mvn spring-boot:run > ../logs/user-service.log 2>&1"
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo    所有服务已启动！
echo ========================================
echo.
echo 服务访问地址：
echo   - OAuth2 认证服务器：http://localhost:8080
echo   - API 网关：http://localhost:8081
echo   - 订单服务：http://localhost:8082
echo   - 用户服务：http://localhost:8083
echo.
echo 测试账号：
echo   - 管理员：admin / 123456
echo   - 普通用户：user / 123456
echo.
echo 日志文件位置：%LOG_DIR%
echo.
echo 按任意键关闭此窗口（不会停止服务）
echo ========================================
pause >nul
