package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 学生端首页控制器
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentDashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取首页数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        Map<String, Object> dashboard = dashboardService.getStudentDashboard(studentId);
        return Result.success(dashboard);
    }
}
