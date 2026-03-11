@echo off
chcp 65001 >nul
echo ========================================
echo    OAuth2 SSO 系统 - 停止所有服务
echo ========================================
echo.

echo 正在停止所有服务...
echo.

REM 停止 Java 进程
taskkill /F /FI "WindowTitle eq OAuth2 Server*" 2>nul
taskkill /F /FI "WindowTitle eq Gateway*" 2>nul
taskkill /F /FI "WindowTitle eq Order Service*" 2>nul
taskkill /F /FI "WindowTitle eq User Service*" 2>nul

REM 也通过进程名停止
taskkill /F /IM java.exe 2>nul

echo.
echo √ 所有服务已停止！
echo.
pause
