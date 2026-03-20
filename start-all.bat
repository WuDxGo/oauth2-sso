@echo off
chcp 65001 >nul
echo ========================================
echo   OAuth2 SSO 前后端分离系统 - 启动脚本
echo ========================================
echo.

cd /d "%~dp0"

REM 查找 Maven
set MAVEN_CMD=mvn
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "%M2_HOME%\bin\mvn.cmd" (
        set MAVEN_CMD=%M2_HOME%\bin\mvn.cmd
    ) else if exist "%M2_HOME%\bin\mvn.bat" (
        set MAVEN_CMD=%M2_HOME%\bin\mvn.bat
    )
)

echo 正在检查 MySQL 服务...
sc query MySQL80 | find "RUNNING" >nul 2>&1
if errorlevel 1 (
    echo [警告] MySQL 服务未运行，请先启动 MySQL！
    echo.
) else (
    echo [OK] MySQL 服务正在运行
)

echo.
echo 正在检查 Redis 服务...
redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo [警告] Redis 服务未运行，请先启动 Redis！
    echo.
) else (
    echo [OK] Redis 服务正在运行
)

echo.
echo ========================================
echo   启动后端服务
echo ========================================
echo.

echo [1/4] 启动 OAuth2 认证服务器 (端口 8080)...
start "OAuth2 Server" cmd /k "cd oauth-server && %MAVEN_CMD% spring-boot:run -Dspring-boot.run.forked=false"
timeout /t 15 /nobreak >nul

echo [2/4] 启动 API 网关 (端口 8081)...
start "API Gateway" cmd /k "cd gateway && %MAVEN_CMD% spring-boot:run -Dspring-boot.run.forked=false"
timeout /t 10 /nobreak >nul

echo [3/4] 启动订单服务 (端口 8082)...
start "Order Service" cmd /k "cd order-service && %MAVEN_CMD% spring-boot:run -Dspring-boot.run.forked=false"
timeout /t 10 /nobreak >nul

echo [4/4] 启动用户服务 (端口 8083)...
start "User Service" cmd /k "cd user-service && %MAVEN_CMD% spring-boot:run -Dspring-boot.run.forked=false"
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   后端服务启动完成！
echo ========================================
echo.
echo 服务端口:
echo   - OAuth2 认证服务器：http://localhost:8080
echo   - API 网关：http://localhost:8081
echo   - 订单服务：http://localhost:8082
echo   - 用户服务：http://localhost:8083
echo.

echo ========================================
echo   启动前端开发服务器
echo ========================================
echo.

echo [前端] 启动 Vite 开发服务器 (端口 3000)...
start "Frontend Dev Server" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo   所有服务已启动！
echo ========================================
echo.
echo 访问地址：http://localhost:3000
echo.
echo 测试账号:
echo   管理员：admin / 123456
echo   普通用户：user / 123456
echo.
echo 按任意键关闭此窗口 (不会停止服务)
pause >nul
