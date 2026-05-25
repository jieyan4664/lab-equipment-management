package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherBorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 老师端借用归还管理控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/borrows")
@RequiredArgsConstructor
public class TeacherBorrowController {
    
    private final TeacherBorrowService teacherBorrowService;
    
    /**
     * 获取当前借用列表
     */
    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentBorrows(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isOverdue) {
        
        List<Map<String, Object>> list = teacherBorrowService.getCurrentBorrows(keyword, isOverdue);
        
        Map<String, Object> data = Map.of("list", list);
        
        return Result.success(data);
    }
    
    /**
     * 借用登记
     */
    @PostMapping
    public Result<Map<String, Object>> createBorrow(@RequestBody Map<String, String> params) {
        String deviceCode = params.get("deviceCode");
        String studentNo = params.get("studentNo");
        String dueTime = params.get("dueTime");
        String remark = params.get("remark");
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        Map<String, Object> result = teacherBorrowService.createBorrow(
                deviceCode, studentNo, dueTime, remark, teacherId);
        
        return Result.success(result);
    }
    
    /**
     * 归还登记
     */
    @PostMapping("/return")
    public Result<Void> returnBorrow(@RequestBody Map<String, String> params) {
        String deviceCode = params.get("deviceCode");
        String equipmentCondition = params.get("equipmentCondition");
        String violationType = params.get("violationType");
        String violationDescription = params.get("violationDescription");
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        teacherBorrowService.returnBorrow(deviceCode, equipmentCondition, 
                                         violationType, violationDescription, teacherId);
        
        return Result.success();
    }
    
    /**
     * 催还通知
     */
    @PostMapping("/{id}/remind")
    public Result<Void> remindReturn(@PathVariable Integer id) {
        teacherBorrowService.remindReturn(id);
        return Result.success();
    }
}
