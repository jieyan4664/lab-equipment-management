package com.lab.backed.service;

import java.util.Map;

/**
 * 老师端公告管理服务接口
 */
public interface TeacherAnnouncementService {
    
    /**
     * 获取公告列表（分页）
     */
    Map<String, Object> getAnnouncementList(Integer page, Integer size);
    
    /**
     * 发布公告
     */
    void createAnnouncement(String title, String content, String targetType,
                           String targetIds, Integer isPinned, Integer teacherId);
    
    /**
     * 删除公告（软删除）
     */
    void deleteAnnouncement(Integer id);
}
