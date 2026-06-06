package com.lab.backed.service;

import java.util.Map;

/**
 * 老师端统计数据服务接口
 */
public interface TeacherStatisticsService {
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getStatistics(String startDate, String endDate);
    
    /**
     * 获取设备借用排行
     */
    Map<String, Object> getDeviceRankings(String startDate, String endDate);
    
    /**
     * 获取设备类别占比
     */
    Map<String, Object> getCategoryRatio(String startDate, String endDate);
    
    /**
     * 获取月度借用趋势
     */
    Map<String, Object> getMonthlyTrend(String startDate, String endDate);
    
    /**
     * 获取学生活跃度排行
     */
    Map<String, Object> getStudentActivity(String startDate, String endDate);
    
    /**
     * 获取违规统计
     */
    Map<String, Object> getViolationStats(String startDate, String endDate);
}
