package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.PageResult;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.DeviceCategory;
import com.lab.backed.mapper.DeviceCategoryMapper;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 设备服务实现
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    
    private final DeviceMapper deviceMapper;
    private final DeviceCategoryMapper categoryMapper;
    
    @Override
    public List<DeviceCategory> getCategories() {
        LambdaQueryWrapper<DeviceCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceCategory::getStatus, 1)
               .orderByAsc(DeviceCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
    
    @Override
    public PageResult<Device> getDevices(Integer categoryId, String status, String keyword, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Device::getCategoryId, categoryId);
        }
        
        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(Device::getStatus, status);
        }
        
        // 关键词搜索（名称、编号、品牌、型号）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Device::getName, keyword)
                             .or()
                             .like(Device::getCode, keyword)
                             .or()
                             .like(Device::getBrand, keyword)
                             .or()
                             .like(Device::getModel, keyword));
        }
        
        // 排序
        wrapper.orderByDesc(Device::getCreatedAt);
        
        // 分页查询
        Page<Device> devicePage = new Page<>(page, size);
        Page<Device> result = deviceMapper.selectPage(devicePage, wrapper);
        
        // 填充分类名称和缩略图
        List<Device> devices = result.getRecords();
        devices.forEach(this::fillDeviceInfo);
        
        return new PageResult<>(result.getTotal(), devices);
    }
    
    @Override
    public Device getDeviceDetail(Integer id) {
        Device device = deviceMapper.selectById(id);
        if (device != null) {
            fillDeviceInfo(device);
        }
        return device;
    }
    
    /**
     * 填充设备扩展信息
     */
    private void fillDeviceInfo(Device device) {
        // 获取分类名称
        if (device.getCategoryId() != null) {
            DeviceCategory category = categoryMapper.selectById(device.getCategoryId());
            if (category != null) {
                device.setCategoryName(category.getName());
            }
        }
        
        // 设置默认缩略图（实际应该从device_image表获取）
        device.setThumbnail("/images/device/default.jpg");
        
        // 收藏状态（实际应该查询用户收藏表）
        device.setIsFavorited(false);
    }
}
