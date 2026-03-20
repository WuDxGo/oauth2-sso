@echo off
chcp 65001 >nul
echo ========================================
echo   OAuth2 SSO 前后端分离系统 - 构建脚本
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
    ) else (
        echo [错误] 未找到 Maven，请先安装 Maven 或设置 M2_HOME 环境变量
        echo.
        echo 可选方案:
        echo 1. 安装 Maven 并添加到 PATH
        echo 2. 设置 M2_HOME 环境变量指向 Maven 安装目录
        echo 3. 使用 IDE(如 IDEA)直接编译运行
        echo.
        pause
        exit /b 1
    )
)

echo 使用 Maven 命令：%MAVEN_CMD%
echo.

echo [1/3] 正在构建后端项目...
echo.
call %MAVEN_CMD% clean install -DskipTests
if errorlevel 1 (
    echo.
    echo [错误] 后端构建失败！
    echo 请检查:
    echo 1. Java 17+ 是否安装
    echo 2. Maven 是否正确配置
    echo 3. 网络连接是否正常
    echo.
    pause
    exit /b 1
)
echo.
echo [完成] 后端构建成功！
echo.

echo [2/3] 正在构建前端项目...
echo.
cd frontend

REM 检查 Node.js
where node >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Node.js，请先安装 Node.js 18+
    pause
    exit /b 1
)

call npm install --registry=https://registry.npmmirror.com
if errorlevel 1 (
    echo.
    echo [错误] 前端依赖安装失败！
    pause
    exit /b 1
)

call npm run build
if errorlevel 1 (
    echo.
    echo [错误] 前端构建失败！
    pause
    exit /b 1
)
cd ..
echo.
echo [完成] 前端构建成功！
echo.

echo [3/3] 复制前端构建产物到后端...
echo.
if exist "gateway\src\main\resources\static" rmdir /s /q "gateway\src\main\resources\static"
if exist "gateway\src\main\resources\templates" rmdir /s /q "gateway\src\main\resources\templates"

mkdir "gateway\src\main\resources\static" 2>nul
xcopy /E /I /Y "frontend\dist\*" "gateway\src\main\resources\static\" >nul

echo.
echo [完成] 前端文件已复制到网关静态资源目录
echo.

echo ========================================
echo   构建完成！
echo ========================================
echo.
echo 接下来请运行 start-all.bat 启动所有服务
echo.
pause
