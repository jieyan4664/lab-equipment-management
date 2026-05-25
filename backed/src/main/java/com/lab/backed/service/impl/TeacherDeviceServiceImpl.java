package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.DeviceCategory;
import com.lab.backed.mapper.DeviceCategoryMapper;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.service.TeacherDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端设备管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherDeviceServiceImpl implements TeacherDeviceService {
    
    private final DeviceMapper deviceMapper;
    private final DeviceCategoryMapper categoryMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public Page<Map<String, Object>> getDeviceList(String keyword, String status, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（设备名称或编号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Device::getName, keyword)
                             .or()
                             .like(Device::getCode, keyword));
        }
        
        // 状态筛选
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Device::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Device::getCreatedAt);
        
        // 分页查询
        Page<Device> devicePage = new Page<>(page, size);
        Page<Device> result = deviceMapper.selectPage(devicePage, wrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> deviceList = result.getRecords().stream().map(d -> {
            Map<String, Object> dMap = new HashMap<>();
            dMap.put("id", d.getId());
            dMap.put("name", d.getName());
            dMap.put("code", d.getCode());
            dMap.put("category", getCategoryName(d.getCategoryId()));
            dMap.put("model", d.getModel());
            dMap.put("location", d.getLocation());
            dMap.put("purchaseDate", d.getPurchaseDate() != null ? d.getPurchaseDate().format(DATE_FORMATTER) : null);
            dMap.put("warrantyDate", d.getWarrantyDate() != null ? d.getWarrantyDate().format(DATE_FORMATTER) : null);
            dMap.put("status", d.getStatus());
            return dMap;
        }).collect(Collectors.toList());
        
        // 构建返回的分页对象
        Page<Map<String, Object>> returnPage = new Page<>(page, size);
        returnPage.setTotal(result.getTotal());
        returnPage.setRecords(deviceList);
        
        return returnPage;
    }
    
    @Override
    @Transactional
    public void createDevice(Device device) {
        // 检查设备编号是否已存在
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getCode, device.getCode());
        Long count = deviceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("设备编号已存在");
        }
        
        // 设置默认值
        if (device.getStatus() == null || device.getStatus().trim().isEmpty()) {
            device.setStatus("available");
        }
        
        deviceMapper.insert(device);
    }
    
    @Override
    @Transactional
    public void updateDevice(Integer id, Device device) {
        // 检查设备是否存在
        Device existing = deviceMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 如果修改了编号，检查新编号是否已被其他设备使用
        if (!existing.getCode().equals(device.getCode())) {
            LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Device::getCode, device.getCode())
                   .ne(Device::getId, id);
            Long count = deviceMapper.selectCount(wrapper);
            if (count > 0) {
                throw new RuntimeException("设备编号已存在");
            }
        }
        
        // 更新设备信息
        device.setId(id);
        deviceMapper.updateById(device);
    }
    
    @Override
    @Transactional
    public void deleteDevice(Integer id) {
        // 检查设备是否存在
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 检查设备是否正在被借用
        if ("borrowed".equals(device.getStatus())) {
            throw new RuntimeException("设备正在被借用，无法删除");
        }
        
        deviceMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void updateDeviceStatus(Integer id, String status, String reason) {
        // 检查设备是否存在
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 验证状态值
        if (!"repair".equals(status) && !"scrap".equals(status)) {
            throw new RuntimeException("无效的状态值");
        }
        
        // 更新状态
        device.setStatus(status);
        
        // 如果是报废，清空当前借用人
        if ("scrap".equals(status)) {
            device.setCurrentBorrowerId(null);
            device.setExpectedReturnTime(null);
        }
        
        deviceMapper.updateById(device);
        
        // TODO: 记录状态变更日志（可以创建一个device_status_log表）
        System.out.println("设备状态变更 - ID: " + id + ", 状态: " + status + ", 原因: " + reason);
    }
    
    @Override
    public Map<String, Object> generateQRCodes(List<Integer> deviceIds) {
        // TODO: 实际应该生成二维码并打包为PDF
        // 这里暂时返回模拟数据
        
        // 验证设备ID
        for (Integer deviceId : deviceIds) {
            Device device = deviceMapper.selectById(deviceId);
            if (device == null) {
                throw new RuntimeException("设备ID " + deviceId + " 不存在");
            }
        }
        
        // 模拟生成PDF URL
        String pdfUrl = "/download/qr-codes-" + System.currentTimeMillis() + ".pdf";
        
        Map<String, Object> result = new HashMap<>();
        result.put("pdfUrl", pdfUrl);
        
        return result;
    }
    
    /**
     * 根据分类ID获取分类名称
     */
    private String getCategoryName(Integer categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        
        DeviceCategory category = categoryMapper.selectById(categoryId);
        return category != null ? category.getName() : "未知分类";
    }
}
