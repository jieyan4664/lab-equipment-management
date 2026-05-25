package com.lab.backed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * 老师端预约审核服务接口
 */
public interface TeacherReservationService {
    
    /**
     * 获取预约列表（分页）
     */
    Page<Map<String, Object>> getReservationList(String status, String studentName, 
                                                  String deviceName, Integer page, Integer size);
    
    /**
     * 审核预约
     */
    void auditReservation(Integer id, String result, String reason, Integer teacherId);
}
