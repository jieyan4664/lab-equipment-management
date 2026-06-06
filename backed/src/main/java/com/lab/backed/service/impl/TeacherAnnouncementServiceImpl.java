package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Announcement;
import com.lab.backed.mapper.AnnouncementMapper;
import com.lab.backed.service.TeacherAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 老师端公告管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherAnnouncementServiceImpl implements TeacherAnnouncementService {
    
    private final AnnouncementMapper announcementMapper;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Map<String, Object> getAnnouncementList(Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的公告
        wrapper.eq(Announcement::getStatus, 1);
        
        // 按置顶排序，然后按发布时间倒序
        wrapper.orderByDesc(Announcement::getIsPinned)
               .orderByDesc(Announcement::getPublishTime);
        
        // 分页查询
        Page<Announcement> announcementPage = new Page<>(page, size);
        Page<Announcement> result = announcementMapper.selectPage(announcementPage, wrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> announcementList = result.getRecords().stream()
                .map(a -> {
                    Map<String, Object> aMap = new HashMap<>();
                    aMap.put("id", a.getId());
                    aMap.put("title", a.getTitle());
                    aMap.put("content", a.getContent());
                    aMap.put("attachments", a.getAttachments());
                    aMap.put("targetType", a.getTargetType());
                    aMap.put("targetIds", a.getTargetIds());
                    aMap.put("isPinned", a.getIsPinned() == 1);
                    aMap.put("publishTime", a.getPublishTime().format(DATETIME_FORMATTER));
                    aMap.put("teacherId", a.getTeacherId());
                    aMap.put("status", a.getStatus());
                    aMap.put("createdAt", a.getCreatedAt().format(DATETIME_FORMATTER));
                    
                    return aMap;
                })
                .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("list", announcementList);
        
        return resultMap;
    }
    
    @Override
    @Transactional
    public void createAnnouncement(String title, String content, String targetType,
                                  String targetIds, Integer isPinned, Integer teacherId) {
        // 验证标题
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("公告标题不能为空");
        }
        
        // 验证内容
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("公告内容不能为空");
        }
        
        // 验证发布范围
        if (targetType == null || targetType.trim().isEmpty()) {
            throw new RuntimeException("请选择发布范围");
        }
        
        // 创建公告
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setAttachments(targetIds); // 如果有附件，可以存储在这里
        announcement.setTargetType(targetType);
        announcement.setTargetIds(targetIds);
        announcement.setIsPinned(isPinned != null && isPinned == 1 ? 1 : 0);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setTeacherId(teacherId);
        announcement.setStatus(1); // 正常状态
        announcement.setCreatedAt(LocalDateTime.now());
        
        announcementMapper.insert(announcement);
    }
    
    @Override
    @Transactional
    public void deleteAnnouncement(Integer id) {
        // 检查公告是否存在
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        
        // 软删除：将状态设置为0
        announcement.setStatus(0);
        announcementMapper.updateById(announcement);
    }
}
