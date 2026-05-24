package com.lab.backed.controller;

import com.lab.backed.common.PageResult;
import com.lab.backed.common.Result;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.DeviceCategory;
import com.lab.backed.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端设备控制器
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentDeviceController {
    
    private final DeviceService deviceService;
    
    /**
     * 获取设备分类列表
     */
    @GetMapping("/categories")
    public Result<List<DeviceCategory>> getCategories() {
        List<DeviceCategory> categories = deviceService.getCategories();
        return Result.success(categories);
    }
    
    /**
     * 获取设备列表（分页）
     */
    @GetMapping("/devices")
    public Result<PageResult<Device>> getDevices(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        
        PageResult<Device> result = deviceService.getDevices(categoryId, status, keyword, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取设备详情
     */
    @GetMapping("/devices/{id}")
    public Result<Device> getDeviceDetail(@PathVariable Integer id) {
        Device device = deviceService.getDeviceDetail(id);
        if (device == null) {
            return Result.error(404, "设备不存在");
        }
        return Result.success(device);
    }
}
