@echo off
REM Freeplane 环境检查脚本 (Windows)
REM 用于验证新电脑上的开发环境是否正确配置

echo ======================================
echo   Freeplane 开发环境检查
echo ======================================
echo.

REM 检查 Java
echo [检查] Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [成功] Java 已安装
    java -version 2>&1 | findstr "version"
) else (
    echo [失败] Java 未安装
    echo   下载地址: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

echo.
echo [检查] JAVA_HOME...
if defined JAVA_HOME (
    echo [成功] JAVA_HOME 已设置: %JAVA_HOME%
) else (
    echo [警告] JAVA_HOME 未设置
    echo   请设置环境变量指向 JDK 21
)

echo.
echo [检查] Git...
git --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [成功] Git 已安装
    git --version
) else (
    echo [失败] Git 未安装
    echo   下载地址: https://git-scm.com/downloads
    pause
    exit /b 1
)

echo.
echo [检查] Gradle Wrapper...
if exist "gradlew.bat" (
    echo [成功] Gradle Wrapper 已存在
) else (
    echo [失败] Gradle Wrapper 不存在
    echo   请从项目仓库克隆
    pause
    exit /b 1
)

echo.
echo [检查] Gradle...
call gradlew.bat -v >nul 2>&1
if %errorlevel% equ 0 (
    echo [成功] Gradle 可用
    call gradlew.bat -v 2>&1 | findstr "Gradle"
) else (
    echo [失败] Gradle 测试失败
    pause
    exit /b 1
)

echo.
echo [检查] 本地依赖...
if exist "freeplane\lib" (
    echo [成功] freeplane\lib\ 存在
) else (
    echo [警告] freeplane\lib\ 不存在
)

echo.
echo ======================================
echo [成功] 环境检查通过！
echo.
echo 下一步：
echo   1. 构建项目: gradlew.bat build
echo   2. 运行程序: BIN\freeplane.bat
echo ======================================
pause
