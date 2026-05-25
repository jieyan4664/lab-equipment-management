package com.lab.backed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;

/**
 * 学生端公告服务接口
 */
public interface AnnouncementService {
    
    /**
     * 获取公告列表（分页）
     */
    Page<Map<String, Object>> getAnnouncementList(Integer studentId, String labType, 
                                                    String filterType, Integer page, Integer size);
    
    /**
     * 获取公告详情
     */
    Map<String, Object> getAnnouncementDetail(Integer announcementId, Integer studentId);
    
    /**
     * 标记公告已读
     */
    void markAsRead(Integer announcementId, Integer studentId);
}
