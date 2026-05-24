package com.lab.backed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.PageResult;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.DeviceCategory;

import java.util.List;

/**
 * 设备服务接口
 */
public interface DeviceService {
    
    /**
     * 获取设备分类列表
     */
    List<DeviceCategory> getCategories();
    
    /**
     * 分页查询设备列表
     */
    PageResult<Device> getDevices(Integer categoryId, String status, String keyword, Integer page, Integer size);
    
    /**
     * 获取设备详情
     */
    Device getDeviceDetail(Integer id);
}
