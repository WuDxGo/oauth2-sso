@echo off
chcp 65001 >nul
echo ========================================
echo    OAuth2 SSO 系统 - 编译脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/2] 正在编译项目...
call mvn clean install -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo ✗ 编译失败！请检查错误信息。
    pause
    exit /b 1
)

echo.
echo √ 编译成功！
echo.
echo ========================================
echo    下一步操作：
echo ========================================
echo 1. 确保 MySQL 已启动并执行了 init.sql 脚本
echo 2. 确保 Redis 已启动（默认端口 6379）
echo 3. 运行 start-all.bat 启动所有服务
echo ========================================
echo.
pause
