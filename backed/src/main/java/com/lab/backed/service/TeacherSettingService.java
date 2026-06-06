package com.lab.backed.service;

import java.util.Map;

/**
 * 系统设置Service接口
 */
public interface TeacherSettingService {
    
    /**
     * 获取所有系统设置
     * @return 设置数据Map
     */
    Map<String, Object> getAllSettings();
    
    /**
     * 更新系统设置
     * @param settings 设置数据
     */
    void updateSettings(Map<String, Object> settings);
}
