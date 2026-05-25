package com.lab.backed.service;

import java.util.Map;

/**
 * 老师端仪表盘服务接口
 */
public interface TeacherDashboardService {
    
    /**
     * 获取仪表盘数据
     */
    Map<String, Object> getDashboardData();
}
