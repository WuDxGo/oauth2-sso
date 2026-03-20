@echo off
chcp 65001 >nul
echo ========================================
echo   OAuth2 SSO 前后端分离系统 - 停止脚本
echo ========================================
echo.

echo 正在停止所有服务...
echo.

echo [1/4] 停止用户服务...
taskkill /F /FI "WINDOWTITLE eq User Service*" 2>nul
timeout /t 2 /nobreak >nul

echo [2/4] 停止订单服务...
taskkill /F /FI "WINDOWTITLE eq Order Service*" 2>nul
timeout /t 2 /nobreak >nul

echo [3/4] 停止 API 网关...
taskkill /F /FI "WINDOWTITLE eq API Gateway*" 2>nul
timeout /t 2 /nobreak >nul

echo [4/4] 停止 OAuth2 认证服务器...
taskkill /F /FI "WINDOWTITLE eq OAuth2 Server*" 2>nul
timeout /t 2 /nobreak >nul

echo.
echo [前端] 停止前端开发服务器...
taskkill /F /FI "WINDOWTITLE eq Frontend Dev Server*" 2>nul
timeout /t 2 /nobreak >nul

echo.
echo ========================================
echo   所有服务已停止！
echo ========================================
echo.
pause
