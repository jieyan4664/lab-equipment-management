package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.RepairRecord;
import com.lab.backed.entity.ScrapRecord;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.RepairRecordMapper;
import com.lab.backed.mapper.ScrapRecordMapper;
import com.lab.backed.service.TeacherRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端维修报废管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherRepairServiceImpl implements TeacherRepairService {
    
    private final RepairRecordMapper repairRecordMapper;
    private final ScrapRecordMapper scrapRecordMapper;
    private final DeviceMapper deviceMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public Map<String, Object> getRepairList(String status, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<RepairRecord> wrapper = new LambdaQueryWrapper<>();
        
        // TODO: 如果需要按状态筛选，需要关联查询设备表
        // 目前repair_record表没有status字段，暂时返回所有记录
        
        // 按创建时间倒序
        wrapper.orderByDesc(RepairRecord::getCreatedAt);
        
        // 分页查询
        Page<RepairRecord> repairPage = new Page<>(page, size);
        Page<RepairRecord> result = repairRecordMapper.selectPage(repairPage, wrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> repairList = result.getRecords().stream()
                .map(r -> {
                    Map<String, Object> rMap = new HashMap<>();
                    rMap.put("id", r.getId());
                    
                    // 获取设备信息
                    Device device = deviceMapper.selectById(r.getDeviceId());
                    rMap.put("deviceName", device != null ? device.getName() : "未知设备");
                    rMap.put("category", device != null ? getCategoryName(device.getCategoryId()) : "未知分类");
                    rMap.put("location", device != null ? device.getLocation() : "未知位置");
                    
                    rMap.put("repairDate", r.getRepairDate().format(DATE_FORMATTER));
                    rMap.put("repairPerson", r.getRepairPerson());
                    rMap.put("cost", r.getCost() != null ? r.getCost() : 0);
                    rMap.put("result", r.getResult());
                    rMap.put("description", r.getDescription());
                    rMap.put("images", r.getImages());
                    
                    return rMap;
                })
                .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("list", repairList);
        
        return resultMap;
    }
    
    @Override
    @Transactional
    public void createRepair(Integer deviceId, String repairDate, String repairPerson,
                            Double cost, String result, String description, 
                            List<String> images, Integer teacherId) {
        // 检查设备是否存在
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 验证维修结果
        if (!"repaired".equals(result) && !"unrepairable".equals(result)) {
            throw new RuntimeException("无效的维修结果");
        }
        
        // 创建维修记录
        RepairRecord record = new RepairRecord();
        record.setDeviceId(deviceId);
        record.setRepairDate(LocalDate.parse(repairDate, DATE_FORMATTER));
        record.setRepairPerson(repairPerson);
        record.setCost(cost != null ? java.math.BigDecimal.valueOf(cost) : java.math.BigDecimal.ZERO);
        record.setResult(result);
        record.setDescription(description);
        
        // 将图片列表转为JSON字符串
        if (images != null && !images.isEmpty()) {
            record.setImages(String.join(",", images));
        }
        
        record.setTeacherId(teacherId);
        record.setCreatedAt(LocalDateTime.now());
        
        repairRecordMapper.insert(record);
        
        // 如果无法修复，自动将设备状态改为报废
        if ("unrepairable".equals(result)) {
            device.setStatus("scrap");
            device.setUpdatedAt(LocalDateTime.now());
            deviceMapper.updateById(device);
        } else if ("repaired".equals(result)) {
            // 如果已修复，将设备状态改回可用
            device.setStatus("available");
            device.setUpdatedAt(LocalDateTime.now());
            deviceMapper.updateById(device);
        }
    }
    
    @Override
    public Map<String, Object> getScrapList(Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<ScrapRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 按创建时间倒序
        wrapper.orderByDesc(ScrapRecord::getCreatedAt);
        
        // 分页查询
        Page<ScrapRecord> scrapPage = new Page<>(page, size);
        Page<ScrapRecord> result = scrapRecordMapper.selectPage(scrapPage, wrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> scrapList = result.getRecords().stream()
                .map(s -> {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("id", s.getId());
                    
                    // 获取设备信息
                    Device device = deviceMapper.selectById(s.getDeviceId());
                    sMap.put("deviceName", device != null ? device.getName() : "未知设备");
                    sMap.put("deviceCode", device != null ? device.getCode() : "未知编号");
                    sMap.put("category", device != null ? getCategoryName(device.getCategoryId()) : "未知分类");
                    
                    sMap.put("scrapDate", s.getScrapDate().format(DATE_FORMATTER));
                    sMap.put("reason", s.getReason());
                    sMap.put("description", s.getDescription());
                    sMap.put("disposal", s.getDisposal());
                    
                    return sMap;
                })
                .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("list", scrapList);
        
        return resultMap;
    }
    
    @Override
    @Transactional
    public void createScrap(Integer deviceId, String scrapDate, String reason,
                           String description, String disposal, Integer teacherId) {
        // 检查设备是否存在
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 验证报废原因
        List<String> validReasons = Arrays.asList("wear", "damage", "obsolete", "other");
        if (!validReasons.contains(reason)) {
            throw new RuntimeException("无效的报废原因");
        }
        
        // 验证处置方式
        List<String> validDisposals = Arrays.asList("keep", "discard", "recycle");
        if (!validDisposals.contains(disposal)) {
            throw new RuntimeException("无效的处置方式");
        }
        
        // 创建报废记录
        ScrapRecord record = new ScrapRecord();
        record.setDeviceId(deviceId);
        record.setScrapDate(LocalDate.parse(scrapDate, DATE_FORMATTER));
        record.setReason(reason);
        record.setDescription(description);
        record.setDisposal(disposal);
        record.setTeacherId(teacherId);
        record.setCreatedAt(LocalDateTime.now());
        
        scrapRecordMapper.insert(record);
        
        // 更新设备状态为报废
        device.setStatus("scrap");
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
    }
    
    /**
     * 根据分类ID获取分类名称
     */
    private String getCategoryName(Integer categoryId) {
        // TODO: 这里可以查询device_category表获取分类名称
        // 暂时返回默认值
        return "设备分类";
    }
}
