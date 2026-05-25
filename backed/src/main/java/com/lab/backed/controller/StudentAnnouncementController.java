package com.lab.backed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.Result;
import com.lab.backed.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生端公告控制器
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentAnnouncementController {
    
    private final AnnouncementService announcementService;
    
    /**
     * 获取公告列表
     */
    @GetMapping("/announcements")
    public Result<Page<Map<String, Object>>> getAnnouncements(
            @RequestParam(required = false, defaultValue = "all") String filterType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // TODO: 从Token中获取学生ID和实验室类型，暂时使用模拟数据
        Integer studentId = 1;
        String labType = "bio";
        
        Page<Map<String, Object>> result = announcementService.getAnnouncementList(
            studentId, labType, filterType, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取公告详情
     */
    @GetMapping("/announcements/{id}")
    public Result<Map<String, Object>> getAnnouncementDetail(@PathVariable Integer id) {
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        Map<String, Object> detail = announcementService.getAnnouncementDetail(id, studentId);
        return Result.success(detail);
    }
    
    /**
     * 标记公告已读
     */
    @PutMapping("/announcements/{id}/read")
    public Result<Void> markAsRead(@PathVariable Integer id) {
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        announcementService.markAsRead(id, studentId);
        return Result.success();
    }
}
