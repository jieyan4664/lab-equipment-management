package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.backed.entity.SystemSetting;
import com.lab.backed.mapper.SystemSettingMapper;
import com.lab.backed.service.TeacherSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 系统设置Service实现类
 */
@Service
public class TeacherSettingServiceImpl implements TeacherSettingService {
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 默认设置值
    private static final Map<String, Object> DEFAULT_SETTINGS = new HashMap<>();
    
    static {
        // 实验室信息
        Map<String, Object> labInfo = new HashMap<>();
        labInfo.put("name", "生物化学实验室");
        labInfo.put("openHours", "周一至周五 08:00-17:00\n周六 09:00-12:00");
        labInfo.put("rules", "1. 进入实验室必须穿戴实验服和防护眼镜\n" +
                "2. 严禁在实验室内饮食、吸烟\n" +
                "3. 使用设备前必须阅读使用说明\n" +
                "4. 使用完毕后清理工作台，归位设备\n" +
                "5. 发现设备故障立即报告管理员\n" +
                "6. 严格遵守操作规程，注意安全\n" +
                "7. 离开实验室前关闭所有设备电源");
        DEFAULT_SETTINGS.put("labInfo", labInfo);
        
        // 预约规则
        Map<String, Object> reservationRules = new HashMap<>();
        reservationRules.put("maxDuration", 3);
        reservationRules.put("maxAdvanceDays", 7);
        reservationRules.put("cancelAdvanceHours", 24);
        reservationRules.put("maxBorrowCount", 3);
        reservationRules.put("slotGranularity", 2);
        DEFAULT_SETTINGS.put("reservationRules", reservationRules);
        
        // 归还提醒
        Map<String, Object> reminderSettings = new HashMap<>();
        reminderSettings.put("overdueThreshold", 24);
        reminderSettings.put("remindMethods", Arrays.asList("sms", "inapp"));
        reminderSettings.put("remindInterval", "daily");
        reminderSettings.put("advanceRemindHours", 2);
        DEFAULT_SETTINGS.put("reminderSettings", reminderSettings);

        // 角色权限
        List<Map<String, Object>> rolePermissions = new ArrayList<>();

        Map<String, Object> teacherRole = new HashMap<>();
        teacherRole.put("roleName", "普通老师");
        teacherRole.put("roleCode", "teacher");
        teacherRole.put("description", "普通老师角色，拥有基本管理权限");
        teacherRole.put("permissions", Arrays.asList(
                "dashboard", "devices", "reservations", "borrows",
                "students", "repairs", "announcements", "statistics", "settings"
        ));
        rolePermissions.add(teacherRole);

        Map<String, Object> adminRole = new HashMap<>();
        adminRole.put("roleName", "管理员");
        adminRole.put("roleCode", "admin");
        adminRole.put("description", "管理员角色，拥有全部管理权限");
        adminRole.put("permissions", Arrays.asList(
                "dashboard", "devices", "reservations", "borrows",
                "students", "repairs", "announcements", "statistics", "settings"
        ));
        rolePermissions.add(adminRole);

        DEFAULT_SETTINGS.put("rolePermissions", rolePermissions);
    }
    
    @Override
    public Map<String, Object> getAllSettings() {
        // 从数据库读取设置
        List<SystemSetting> settings = systemSettingMapper.selectList(null);
        
        // 如果数据库为空，初始化默认设置
        if (settings == null || settings.isEmpty()) {
            initializeDefaultSettings();
            return DEFAULT_SETTINGS;
        }
        
        // 解析设置数据
        Map<String, Object> result = new HashMap<>();
        for (SystemSetting setting : settings) {
            try {
                Object value = objectMapper.readValue(setting.getSettingValue(), Object.class);
                result.put(setting.getSettingKey(), value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        
        // 补充缺失的默认设置
        for (Map.Entry<String, Object> entry : DEFAULT_SETTINGS.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public void updateSettings(Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            try {
                // 将值转换为JSON字符串
                String jsonValue = objectMapper.writeValueAsString(value);
                
                // 查询是否已存在
                LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SystemSetting::getSettingKey, key);
                SystemSetting existing = systemSettingMapper.selectOne(wrapper);
                
                if (existing != null) {
                    // 更新
                    existing.setSettingValue(jsonValue);
                    existing.setUpdatedAt(LocalDateTime.now());
                    systemSettingMapper.updateById(existing);
                } else {
                    // 新增
                    SystemSetting newSetting = new SystemSetting();
                    newSetting.setSettingKey(key);
                    newSetting.setSettingValue(jsonValue);
                    newSetting.setDescription(getDescriptionByKey(key));
                    newSetting.setUpdatedAt(LocalDateTime.now());
                    systemSettingMapper.insert(newSetting);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException("设置保存失败: " + key);
            }
        }
    }
    
    /**
     * 初始化默认设置
     */
    private void initializeDefaultSettings() {
        for (Map.Entry<String, Object> entry : DEFAULT_SETTINGS.entrySet()) {
            try {
                String jsonValue = objectMapper.writeValueAsString(entry.getValue());
                SystemSetting setting = new SystemSetting();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(jsonValue);
                setting.setDescription(getDescriptionByKey(entry.getKey()));
                setting.setUpdatedAt(LocalDateTime.now());
                systemSettingMapper.insert(setting);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 根据key获取描述
     */
    private String getDescriptionByKey(String key) {
        switch (key) {
            case "labInfo":
                return "实验室基本信息配置";
            case "reservationRules":
                return "预约规则配置";
            case "reminderSettings":
                return "归还提醒配置";
            case "rolePermissions":
                return "角色权限配置";
            default:
                return "系统设置";
        }
    }
}