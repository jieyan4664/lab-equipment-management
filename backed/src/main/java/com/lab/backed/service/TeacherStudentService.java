package com.lab.backed.service;

import java.util.Map;

/**
 * 老师端学生管理服务接口
 */
public interface TeacherStudentService {
    
    /**
     * 获取学生列表（分页）
     */
    Map<String, Object> getStudentList(String className, String keyword, Integer page, Integer size);
    
    /**
     * 更新学生信息
     */
    void updateStudent(Integer id, String className, String phone, String email);
    
    /**
     * 禁用/启用学生权限
     */
    void updateAccessStatus(Integer id, Integer status, String reason, Integer banDays);
}
