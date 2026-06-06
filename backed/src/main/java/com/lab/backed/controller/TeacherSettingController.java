package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherSettingController {

    private final TeacherSettingService teacherSettingService;

    @GetMapping("/settings")
    public Result<Map<String, Object>> getSettings() {
        Map<String, Object> settings = teacherSettingService.getAllSettings();
        return Result.success(settings);
    }

    @PutMapping("/settings")
    public Result<Void> updateSettings(@RequestBody Map<String, Object> settings) {
        teacherSettingService.updateSettings(settings);
        return Result.success();
    }
}