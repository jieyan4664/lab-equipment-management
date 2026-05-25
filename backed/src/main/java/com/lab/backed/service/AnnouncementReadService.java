package com.lab.backed.service;

import java.util.Set;

/**
 * 公告已读状态服务
 * 用于跟踪学生对公告的已读状态
 */
public interface AnnouncementReadService {
    
    /**
     * 标记公告为已读
     */
    void markAsRead(Integer studentId, Integer announcementId);
    
    /**
     * 检查公告是否已读
     */
    boolean isRead(Integer studentId, Integer announcementId);
    
    /**
     * 获取学生已读的公告ID集合
     */
    Set<Integer> getReadAnnouncementIds(Integer studentId);
}
