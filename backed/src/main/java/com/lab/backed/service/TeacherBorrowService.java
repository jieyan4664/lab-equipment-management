package com.lab.backed.service;

import java.util.List;
import java.util.Map;

/**
 * 老师端借用归还管理服务接口
 */
public interface TeacherBorrowService {
    
    /**
     * 获取当前借用列表
     */
    List<Map<String, Object>> getCurrentBorrows(String keyword, Boolean isOverdue);
    
    /**
     * 借用登记
     */
    Map<String, Object> createBorrow(String deviceCode, String studentNo, 
                                     String dueTime, String remark, Integer teacherId);
    
    /**
     * 归还登记
     */
    void returnBorrow(String deviceCode, String equipmentCondition, 
                     String violationType, String violationDescription, Integer teacherId);
    
    /**
     * 催还通知
     */
    void remindReturn(Integer borrowId);
}
