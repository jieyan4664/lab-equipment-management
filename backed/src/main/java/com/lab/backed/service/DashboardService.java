package com.lab.backed.service;

import java.util.Map;

/**
 * 学生首页服务接口
 */
public interface DashboardService {
    
    /**
     * 获取学生首页数据
     */
    Map<String, Object> getStudentDashboard(Integer studentId);
}
