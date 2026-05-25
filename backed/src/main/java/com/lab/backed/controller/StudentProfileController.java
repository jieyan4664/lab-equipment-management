package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生端个人中心控制器
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentProfileController {
    
    private final ProfileService profileService;
    
    /**
     * 获取个人中心数据
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        Map<String, Object> profile = profileService.getStudentProfile(studentId);
        return Result.success(profile);
    }
    
    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> updates) {
        try {
            // TODO: 从Token中获取学生ID，暂时使用模拟数据
            Integer studentId = 1;
            
            boolean success = profileService.updateStudentProfile(studentId, updates);
            if (success) {
                return Result.success();
            } else {
                return Result.error(400, "更新失败");
            }
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
