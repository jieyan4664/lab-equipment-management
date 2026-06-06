package com.lab.backed.service;

import java.util.List;
import java.util.Map;

/**
 * 老师端维修报废管理服务接口
 */
public interface TeacherRepairService {
    
    /**
     * 获取维修列表（分页）
     */
    Map<String, Object> getRepairList(String status, Integer page, Integer size);
    
    /**
     * 登记维修
     */
    void createRepair(Integer deviceId, String repairDate, String repairPerson,
                     Double cost, String result, String description, 
                     List<String> images, Integer teacherId);
    
    /**
     * 获取报废列表（分页）
     */
    Map<String, Object> getScrapList(Integer page, Integer size);
    
    /**
     * 登记报废
     */
    void createScrap(Integer deviceId, String scrapDate, String reason,
                    String description, String disposal, Integer teacherId);
}
