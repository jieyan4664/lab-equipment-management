package com.lab.backed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.Result;
import com.lab.backed.entity.Device;
import com.lab.backed.service.TeacherDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 老师端设备管理控制器
 */
@RestController
@RequestMapping("/api/v1/teacher/devices")
@RequiredArgsConstructor
public class TeacherDeviceController {
    
    private final TeacherDeviceService teacherDeviceService;
    
    /**
     * 获取设备列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> getDeviceList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Map<String, Object>> result = teacherDeviceService.getDeviceList(keyword, status, page, size);
        
        Map<String, Object> data = Map.of(
            "total", result.getTotal(),
            "list", result.getRecords()
        );
        
        return Result.success(data);
    }
    
    /**
     * 添加设备
     */
    @PostMapping
    public Result<Void> createDevice(@RequestBody Device device) {
        teacherDeviceService.createDevice(device);
        return Result.success();
    }
    
    /**
     * 更新设备
     */
    @PutMapping("/{id}")
    public Result<Void> updateDevice(@PathVariable Integer id, @RequestBody Device device) {
        teacherDeviceService.updateDevice(id, device);
        return Result.success();
    }
    
    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@PathVariable Integer id) {
        teacherDeviceService.deleteDevice(id);
        return Result.success();
    }
    
    /**
     * 修改设备状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateDeviceStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> params) {
        
        String status = params.get("status");
        String reason = params.get("reason");
        
        teacherDeviceService.updateDeviceStatus(id, status, reason);
        return Result.success();
    }
    
    /**
     * 生成设备二维码
     */
    @PostMapping("/qr-codes")
    public Result<Map<String, Object>> generateQRCodes(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> deviceIds = params.get("deviceIds");
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Result.error("请选择要生成二维码的设备");
        }
        Map<String, Object> result = teacherDeviceService.generateQRCodes(deviceIds);
        return Result.success(result);
    }
}
