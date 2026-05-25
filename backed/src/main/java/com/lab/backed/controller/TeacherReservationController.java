package com.lab.backed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 老师端预约审核控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/reservations")
@RequiredArgsConstructor
public class TeacherReservationController {
    
    private final TeacherReservationService teacherReservationService;
    
    /**
     * 获取预约列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String deviceName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Map<String, Object>> result = teacherReservationService.getReservationList(
                status, studentName, deviceName, page, size);
        
        Map<String, Object> data = Map.of(
            "total", result.getTotal(),
            "list", result.getRecords()
        );
        
        return Result.success(data);
    }
    
    /**
     * 审核预约
     */
    @PutMapping("/{id}/audit")
    public Result<Void> auditReservation(
            @PathVariable Integer id,
            @RequestBody Map<String, String> params) {
        
        String result = params.get("result");
        String reason = params.get("reason");
        
        // TODO: 从token中获取当前老师ID，暂时使用固定值
        Integer teacherId = 1;
        
        teacherReservationService.auditReservation(id, result, reason, teacherId);
        return Result.success();
    }
}
