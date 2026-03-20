@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   OAuth2 SSO 前后端分离系统 - 一键部署
echo ========================================
echo.

cd /d "%~dp0"

REM ===============================
REM 1. 检查环境
REM ===============================
echo [1/6] 检查运行环境...
echo.

REM 检查 Java
where java >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java，请安装 Java 17+ 并添加到 PATH
    pause
    exit /b 1
)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%i
echo [OK] Java 已安装：%JAVA_VER%

REM 检查 Node.js
where node >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Node.js，请安装 Node.js 18+
    pause
    exit /b 1
)
for /f "tokens=2" %%i in ('node -v 2^>^&1') do set NODE_VER=%%i
echo [OK] Node.js 已安装：%NODE_VER%

REM 查找 Maven
set MAVEN_CMD=
where mvn >nul 2>&1 && set MAVEN_CMD=mvn
if "!MAVEN_CMD!"=="" (
    if defined M2_HOME (
        if exist "%M2_HOME%\bin\mvn.cmd" set MAVEN_CMD=%M2_HOME%\bin\mvn.cmd
    )
)
if "!MAVEN_CMD!"=="" (
    REM 常见 Maven 安装路径
    for %%p in (
        "C:\Program Files\Apache\maven\bin\mvn.cmd"
        "D:\Program Files\Apache\maven\bin\mvn.cmd"
        "C:\apache-maven\bin\mvn.cmd"
        "D:\apache-maven\bin\mvn.cmd"
        "C:\ProgramData\chocolatey\bin\mvn.cmd"
    ) do (
        if exist %%p set MAVEN_CMD=%%p
    )
)

if "!MAVEN_CMD!"=="" (
    echo.
    echo [警告] 未找到 Maven，将使用项目中的 Maven Wrapper
    echo.
    echo 正在下载 Maven Wrapper...
    
    if not exist ".mvn\wrapper" mkdir .mvn\wrapper
    
    REM 创建 maven-wrapper.properties
    (
        echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
    ) > .mvn\wrapper\maven-wrapper.properties
    
    REM 下载 mvnw.cmd
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/apache/maven-mvnd/master/mvnw.cmd' -OutFile 'mvnw.cmd' -UseBasicParsing" 2>nul
    
    if exist "mvnw.cmd" (
        set MAVEN_CMD=mvnw.cmd
        echo [OK] Maven Wrapper 已创建
    ) else (
        echo [错误] 无法创建 Maven Wrapper
        echo.
        echo 请手动安装 Maven:
        echo 1. 下载 https://maven.apache.org/download.cgi
        echo 2. 解压到任意目录
        echo 3. 设置 M2_HOME 环境变量
        echo 4. 将 %%M2_HOME%%\bin 添加到 PATH
        pause
        exit /b 1
    )
)

echo [OK] Maven 命令：!MAVEN_CMD!
echo.

REM ===============================
REM 2. 检查数据库
REM ===============================
echo [2/6] 检查数据库服务...
echo.

sc query MySQL80 | find "RUNNING" >nul 2>&1
if errorlevel 1 (
    sc query MySQL | find "RUNNING" >nul 2>&1
    if errorlevel 1 (
        echo [警告] MySQL 服务未运行
        echo 请先启动 MySQL 服务，然后按任意键继续...
        pause >nul
    ) else (
        echo [OK] MySQL 服务正在运行
    )
) else (
    echo [OK] MySQL80 服务正在运行
)

REM 检查 Redis
redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo [警告] Redis 服务未运行
    echo 请先启动 Redis 服务，然后按任意键继续...
    pause >nul
) else (
    echo [OK] Redis 服务正在运行
)
echo.

REM ===============================
REM 3. 初始化数据库
REM ===============================
echo [3/6] 初始化数据库...
echo.
set /p INIT_DB="是否执行数据库初始化脚本？(Y/N，首次运行建议选 Y): "
if /i "!INIT_DB!"=="Y" (
    echo 正在执行 init.sql...
    echo 请输入 MySQL root 密码:
    set /p MYSQL_PWD="密码："
    mysql -u root -p!MYSQL_PWD! < init.sql 2>nul
    if errorlevel 1 (
        echo [警告] 数据库初始化可能失败，请手动执行 init.sql
    ) else (
        echo [OK] 数据库初始化完成
    )
) else (
    echo [跳过] 数据库初始化
)
echo.

REM ===============================
REM 4. 构建后端
REM ===============================
echo [4/6] 构建后端项目...
echo.
call !MAVEN_CMD! clean install -DskipTests -q
if errorlevel 1 (
    echo [错误] 后端构建失败
    pause
    exit /b 1
)
echo [OK] 后端构建成功
echo.

REM ===============================
REM 5. 构建前端
REM ===============================
echo [5/6] 构建前端项目...
echo.
cd frontend
call npm install --registry=https://registry.npmmirror.com -q
if errorlevel 1 (
    echo [错误] 前端依赖安装失败
    pause
    exit /b 1
)
call npm run build
if errorlevel 1 (
    echo [错误] 前端构建失败
    pause
    exit /b 1
)
cd ..
echo [OK] 前端构建成功
echo.

REM ===============================
REM 6. 部署前端到网关
REM ===============================
echo [6/6] 部署前端到网关...
echo.
if exist "gateway\src\main\resources\static" rmdir /s /q "gateway\src\main\resources\static"
mkdir "gateway\src\main\resources\static" 2>nul
xcopy /E /I /Y "frontend\dist\*" "gateway\src\main\resources\static\" >nul
echo [OK] 前端文件已部署到网关
echo.

echo ========================================
echo   部署完成！
echo ========================================
echo.
echo 接下来运行 start-all.bat 启动所有服务
echo.
echo 或者使用 IDE 直接运行各服务的启动类:
echo   - oauth-server/OAuth2ServerApplication
echo   - gateway/GatewayApplication
echo   - order-service/OrderServiceApplication
echo   - user-service/UserServiceApplication
echo.
pause
