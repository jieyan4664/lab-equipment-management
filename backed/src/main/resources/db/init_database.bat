@echo off
chcp 65001 >nul
echo ========================================
echo 实验室设备管理系统 - 数据库初始化
echo ========================================
echo.

set /p MYSQL_USER="请输入MySQL用户名 (默认root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="请输入MySQL密码: "

set DB_NAME=lab-equipment-management
set SCRIPT_DIR=%~dp0

echo.
echo 正在初始化数据库: %DB_NAME%
echo.

echo [1/2] 创建表结构...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% < "%SCRIPT_DIR%schema.sql"
if errorlevel 1 (
    echo 错误: 表结构创建失败！
    pause
    exit /b 1
)
echo 表结构创建成功！
echo.

echo [2/2] 插入模拟数据...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% < "%SCRIPT_DIR%data.sql"
if errorlevel 1 (
    echo 错误: 模拟数据插入失败！
    pause
    exit /b 1
)
echo 模拟数据插入成功！
echo.

echo ========================================
echo 数据库初始化完成！
echo ========================================
echo.
echo 测试账号信息：
echo   学生账号: 2024001-2024010 (密码: 123456)
echo   老师账号: T001-T003 (密码: 123456)
echo.
pause
