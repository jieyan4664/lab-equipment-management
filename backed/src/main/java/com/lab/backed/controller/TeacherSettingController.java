package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 老师端系统设置控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/settings")
@RequiredArgsConstructor
public class TeacherSettingController {

    private final TeacherSettingService teacherSettingService;

    /**
     * 获取所有系统设置
     */
    @GetMapping
    public Result<Map<String, Object>> getSettings() {
        Map<String, Object> settings = teacherSettingService.getAllSettings();
        return Result.success(settings);
    }

    /**
     * 更新系统设置
     */
    @PutMapping
    public Result<Void> updateSettings(@RequestBody Map<String, Object> settings) {
        teacherSettingService.updateSettings(settings);
        return Result.success();
    }
}