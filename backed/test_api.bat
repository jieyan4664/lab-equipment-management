@echo off
chcp 65001 >nul
echo ========================================
echo   后端API测试脚本
echo ========================================
echo.

echo [1] 测试获取设备分类...
curl -s http://localhost:8080/api/v1/student/categories | jq .
echo.
echo ----------------------------------------
echo.

echo [2] 测试获取设备列表（第1页，每页12条）...
curl -s "http://localhost:8080/api/v1/student/devices?page=1&size=12" | jq .
echo.
echo ----------------------------------------
echo.

echo [3] 测试获取设备列表（筛选生物设备）...
curl -s "http://localhost:8080/api/v1/student/devices?categoryId=1&page=1&size=5" | jq .
echo.
echo ----------------------------------------
echo.

echo [4] 测试获取设备列表（搜索关键词"显微镜"）...
curl -s "http://localhost:8080/api/v1/student/devices?keyword=显微镜&page=1&size=5" | jq .
echo.
echo ----------------------------------------
echo.

echo [5] 测试获取设备详情（ID=1）...
curl -s http://localhost:8080/api/v1/student/devices/1 | jq .
echo.
echo ----------------------------------------
echo.

echo 测试完成！
pause
