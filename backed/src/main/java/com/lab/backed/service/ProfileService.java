package com.lab.backed.service;

import com.lab.backed.entity.Student;
import java.util.Map;

/**
 * 学生个人中心服务接口
 */
public interface ProfileService {
    
    /**
     * 获取学生个人资料（包含违规记录）
     */
    Map<String, Object> getStudentProfile(Integer studentId);
    
    /**
     * 更新学生个人资料
     */
    boolean updateStudentProfile(Integer studentId, Map<String, String> updates);
}
