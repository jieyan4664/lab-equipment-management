package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 老师端维修报废管理控制器
 */
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherRepairController {
    
    private final TeacherRepairService teacherRepairService;
    
    /**
     * 获取维修列表（分页）
     */
    @GetMapping("/repairs")
    public Result<Map<String, Object>> getRepairs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> result = teacherRepairService.getRepairList(status, page, size);
        return Result.success(result);
    }
    
    /**
     * 登记维修
     */
    @PostMapping("/repairs")
    public Result<Void> createRepair(@RequestBody Map<String, Object> params) {
        Integer deviceId = (Integer) params.get("deviceId");
        String repairDate = (String) params.get("repairDate");
        String repairPerson = (String) params.get("repairPerson");
        Double cost = params.get("cost") != null ? 
                     ((Number) params.get("cost")).doubleValue() : 0.0;
        String result = (String) params.get("result");
        String description = (String) params.get("description");
        
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) params.get("images");
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        teacherRepairService.createRepair(deviceId, repairDate, repairPerson, 
                                         cost, result, description, images, teacherId);
        return Result.success();
    }
    
    /**
     * 获取报废列表（分页）
     */
    @GetMapping("/scraps")
    public Result<Map<String, Object>> getScraps(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Map<String, Object> result = teacherRepairService.getScrapList(page, size);
        return Result.success(result);
    }
    
    /**
     * 登记报废
     */
    @PostMapping("/scraps")
    public Result<Void> createScrap(@RequestBody Map<String, Object> params) {
        Integer deviceId = (Integer) params.get("deviceId");
        String scrapDate = (String) params.get("scrapDate");
        String reason = (String) params.get("reason");
        String description = (String) params.get("description");
        String disposal = (String) params.get("disposal");
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        teacherRepairService.createScrap(deviceId, scrapDate, reason, 
                                        description, disposal, teacherId);
        return Result.success();
    }
}
