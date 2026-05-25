package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 老师端仪表盘控制器
 */
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherDashboardController {
    
    private final TeacherDashboardService teacherDashboardService;
    
    /**
     * 获取仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboardData = teacherDashboardService.getDashboardData();
        return Result.success(dashboardData);
    }
}
