package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 老师端学生管理控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/students")
@RequiredArgsConstructor
public class TeacherStudentController {
    
    private final TeacherStudentService teacherStudentService;
    
    /**
     * 获取学生列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getStudents(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> result = teacherStudentService.getStudentList(
                className, keyword, page, size);
        
        return Result.success(result);
    }
    
    /**
     * 更新学生信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateStudent(
            @PathVariable Integer id,
            @RequestBody Map<String, String> params) {
        
        String className = params.get("class");
        String phone = params.get("phone");
        String email = params.get("email");
        
        teacherStudentService.updateStudent(id, className, phone, email);
        return Result.success();
    }
    
    /**
     * 禁用/启用学生权限
     */
    @PutMapping("/{id}/access")
    public Result<Void> updateAccessStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        Integer status = (Integer) params.get("status");
        String reason = (String) params.get("reason");
        Integer banDays = params.get("banDays") != null ? 
                         ((Number) params.get("banDays")).intValue() : null;
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        // Integer teacherId = getCurrentTeacherId();
        
        teacherStudentService.updateAccessStatus(id, status, reason, banDays);
        return Result.success();
    }
}
