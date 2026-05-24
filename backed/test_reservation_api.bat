@echo off
chcp 65001 >nul
echo ========================================
echo   学生端预约功能 API 测试
echo ========================================
echo.

set BASE_URL=http://localhost:8080/api/v1/student

echo [测试1] 提交预约申请
echo POST %BASE_URL%/reservations
curl -X POST "%BASE_URL%/reservations" ^
  -H "Content-Type: application/json" ^
  -d "{\"deviceId\":1,\"startTime\":\"2026-01-25 08:00:00\",\"endTime\":\"2026-01-25 12:00:00\",\"purpose\":\"细胞观察实验\"}"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [测试2] 获取当前预约列表
echo GET %BASE_URL%/reservations?type=current^&page=1^&size=10
curl "%BASE_URL%/reservations?type=current&page=1&size=10"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [测试3] 获取历史预约列表
echo GET %BASE_URL%/reservations?type=history^&page=1^&size=10
curl "%BASE_URL%/reservations?type=history&page=1&size=10"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [测试4] 按状态筛选预约
echo GET %BASE_URL%/reservations?type=current^&status=pending^&page=1^&size=10
curl "%BASE_URL%/reservations?type=current&status=pending&page=1&size=10"
echo.
echo.

echo ========================================
echo   测试完成！
echo ========================================
pause
