package com.lab.backed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Device;

import java.util.List;
import java.util.Map;

/**
 * 老师端设备管理服务接口
 */
public interface TeacherDeviceService {
    
    /**
     * 获取设备列表（分页）
     */
    Page<Map<String, Object>> getDeviceList(String keyword, String status, Integer page, Integer size);
    
    /**
     * 添加设备
     */
    void createDevice(Device device);
    
    /**
     * 更新设备
     */
    void updateDevice(Integer id, Device device);
    
    /**
     * 删除设备
     */
    void deleteDevice(Integer id);
    
    /**
     * 修改设备状态
     */
    void updateDeviceStatus(Integer id, String status, String reason);
    
    /**
     * 生成设备二维码
     */
    Map<String, Object> generateQRCodes(List<Integer> deviceIds);
}
