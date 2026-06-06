package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 老师端统计数据控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/statistics")
@RequiredArgsConstructor
public class TeacherStatisticsController {
    
    private final TeacherStatisticsService teacherStatisticsService;
    
    /**
     * 获取统计数据
     */
    @GetMapping
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> result = teacherStatisticsService.getStatistics(startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取设备借用排行
     */
    @GetMapping("/device-rankings")
    public Result<Map<String, Object>> getDeviceRankings(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> result = teacherStatisticsService.getDeviceRankings(startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取设备类别占比
     */
    @GetMapping("/category-ratio")
    public Result<Map<String, Object>> getCategoryRatio(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> result = teacherStatisticsService.getCategoryRatio(startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取月度借用趋势
     */
    @GetMapping("/monthly-trend")
    public Result<Map<String, Object>> getMonthlyTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> result = teacherStatisticsService.getMonthlyTrend(startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取学生活跃度排行
     */
    @GetMapping("/student-activity")
    public Result<Map<String, Object>> getStudentActivity(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> result = teacherStatisticsService.getStudentActivity(startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取违规统计
     */
    @GetMapping("/violation-stats")
    public Result<Map<String, Object>> getViolationStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> result = teacherStatisticsService.getViolationStats(startDate, endDate);
        return Result.success(result);
    }
}
