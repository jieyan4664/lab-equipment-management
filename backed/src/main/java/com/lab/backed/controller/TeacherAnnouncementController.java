package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherAnnouncementService;
import com.lab.backed.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 老师端公告管理控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/announcements")
@RequiredArgsConstructor
public class TeacherAnnouncementController {

    private final TeacherAnnouncementService teacherAnnouncementService;

    @GetMapping
    public Result<Map<String, Object>> getAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = teacherAnnouncementService.getAnnouncementList(page, size);
        return Result.success(result);
    }

    @PostMapping
    public Result<Void> createAnnouncement(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String targetType = (String) params.get("targetType");
        String targetIds = (String) params.get("targetIds");
        Integer isPinned = params.get("isPinned") != null &&
                          ((Boolean) params.get("isPinned")) ? 1 : 0;

        Integer teacherId = UserContext.getUserId();

        teacherAnnouncementService.createAnnouncement(title, content, targetType,
                                                     targetIds, isPinned, teacherId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Integer id) {
        teacherAnnouncementService.deleteAnnouncement(id);
        return Result.success();
    }
}