package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Announcement;
import com.lab.backed.entity.AnnouncementRead;
import com.lab.backed.entity.Teacher;
import com.lab.backed.mapper.AnnouncementMapper;
import com.lab.backed.mapper.AnnouncementReadMapper;
import com.lab.backed.mapper.TeacherMapper;
import com.lab.backed.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生端公告服务实现
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
    
    private final AnnouncementMapper announcementMapper;
    private final TeacherMapper teacherMapper;
    private final AnnouncementReadMapper announcementReadMapper;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public Page<Map<String, Object>> getAnnouncementList(Integer studentId, String labType, 
                                                          String filterType, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)  // 正常状态
              .and(w -> w.eq(Announcement::getTargetType, "all")  // 全部
                       .or()
                       .eq(Announcement::getTargetType, labType))  // 对应实验室
              .orderByDesc(Announcement::getIsPinned)  // 置顶优先
              .orderByDesc(Announcement::getPublishTime);  // 时间倒序
        
        // 分页查询
        Page<Announcement> announcementPage = new Page<>(page, size);
        Page<Announcement> result = announcementMapper.selectPage(announcementPage, wrapper);
        
        // 查询该学生对这些公告的已读状态
        Set<Integer> readAnnouncementIds = getReadAnnouncementIds(studentId);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> announcementList = result.getRecords().stream().map(a -> {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("id", a.getId());
            aMap.put("title", a.getTitle());
            aMap.put("publishTime", a.getPublishTime().format(DATETIME_FORMATTER));
            aMap.put("isPinned", a.getIsPinned() == 1);
            aMap.put("isRead", readAnnouncementIds.contains(a.getId()));  // 查询真实的已读状态
            return aMap;
        }).collect(Collectors.toList());
        
        // 根据筛选类型过滤
        if ("unread".equals(filterType)) {
            announcementList = announcementList.stream()
                .filter(a -> !(Boolean) a.get("isRead"))
                .collect(Collectors.toList());
        } else if ("read".equals(filterType)) {
            announcementList = announcementList.stream()
                .filter(a -> (Boolean) a.get("isRead"))
                .collect(Collectors.toList());
        }
        
        // 构建返回的分页对象
        Page<Map<String, Object>> returnPage = new Page<>(page, size);
        returnPage.setTotal(announcementList.size());
        returnPage.setRecords(announcementList);
        
        return returnPage;
    }
    
    @Override
    public Map<String, Object> getAnnouncementDetail(Integer announcementId, Integer studentId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", announcement.getId());
        detail.put("title", announcement.getTitle());
        detail.put("content", announcement.getContent());
        detail.put("publishTime", announcement.getPublishTime().format(DATETIME_FORMATTER));
        detail.put("isPinned", announcement.getIsPinned() == 1);
        detail.put("attachments", announcement.getAttachments());
        detail.put("teacherName", getTeacherName(announcement.getTeacherId()));
        
        // 标记为已读
        markAsRead(announcementId, studentId);
        detail.put("isRead", true);
        
        return detail;
    }
    
    @Override
    public void markAsRead(Integer announcementId, Integer studentId) {
        // 查询是否已有记录
        LambdaQueryWrapper<AnnouncementRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementRead::getStudentId, studentId)
               .eq(AnnouncementRead::getAnnouncementId, announcementId);
        
        AnnouncementRead existing = announcementReadMapper.selectOne(wrapper);
        
        if (existing != null) {
            // 更新为已读
            existing.setIsRead(1);
            existing.setReadTime(LocalDateTime.now());
            announcementReadMapper.updateById(existing);
        } else {
            // 创建新记录
            AnnouncementRead announcementRead = new AnnouncementRead();
            announcementRead.setStudentId(studentId);
            announcementRead.setAnnouncementId(announcementId);
            announcementRead.setIsRead(1);
            announcementRead.setReadTime(LocalDateTime.now());
            announcementReadMapper.insert(announcementRead);
        }
    }
    
    /**
     * 获取学生已读的公告ID集合
     */
    private Set<Integer> getReadAnnouncementIds(Integer studentId) {
        LambdaQueryWrapper<AnnouncementRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnnouncementRead::getStudentId, studentId)
               .eq(AnnouncementRead::getIsRead, 1);
        
        List<AnnouncementRead> readRecords = announcementReadMapper.selectList(wrapper);
        return readRecords.stream()
                .map(AnnouncementRead::getAnnouncementId)
                .collect(Collectors.toSet());
    }
    
    /**
     * 根据老师ID获取老师姓名
     */
    private String getTeacherName(Integer teacherId) {
        if (teacherId == null) {
            return "管理员";
        }
        
        Teacher teacher = teacherMapper.selectById(teacherId);
        return teacher != null ? teacher.getName() : "管理员";
    }
}
