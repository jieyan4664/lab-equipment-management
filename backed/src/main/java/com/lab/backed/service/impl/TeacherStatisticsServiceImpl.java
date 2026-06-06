package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.DeviceCategory;
import com.lab.backed.entity.Student;
import com.lab.backed.entity.Violation;
import com.lab.backed.mapper.BorrowRecordMapper;
import com.lab.backed.mapper.DeviceCategoryMapper;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.mapper.ViolationMapper;
import com.lab.backed.service.TeacherStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端统计数据服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherStatisticsServiceImpl implements TeacherStatisticsService {
    
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;
    private final DeviceCategoryMapper deviceCategoryMapper;
    private final StudentMapper studentMapper;
    private final ViolationMapper violationMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        
        // 设备借用统计
        result.put("deviceStats", getDeviceRankings(startDate, endDate));
        result.put("categoryRatio", getCategoryRatio(startDate, endDate).get("ratio"));
        result.put("monthlyTrend", getMonthlyTrend(startDate, endDate).get("trend"));
        
        // 学生活跃度
        result.put("studentStats", getStudentActivity(startDate, endDate));
        
        // 违规统计
        result.put("violationStats", getViolationStats(startDate, endDate));
        
        return result;
    }
    
    @Override
    public Map<String, Object> getDeviceRankings(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 时间范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(BorrowRecord::getBorrowTime, LocalDateTime.parse(startDate + " 00:00:00", DATETIME_FORMATTER));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(BorrowRecord::getBorrowTime, LocalDateTime.parse(endDate + " 23:59:59", DATETIME_FORMATTER));
        }
        
        // 查询所有借用记录
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        // 按设备ID分组统计
        Map<Integer, Long> deviceCountMap = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getDeviceId, Collectors.counting()));
        
        // 获取设备信息并排序
        List<Map<String, Object>> rankings = deviceCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10) // 取TOP10
                .map(entry -> {
                    Device device = deviceMapper.selectById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", device != null ? device.getName() : "未知设备");
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        result.put("rankings", rankings);
        return result;
    }
    
    @Override
    public Map<String, Object> getCategoryRatio(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有设备
        List<Device> devices = deviceMapper.selectList(null);
        
        // 按分类ID分组
        Map<Integer, Long> categoryCountMap = devices.stream()
                .collect(Collectors.groupingBy(Device::getCategoryId, Collectors.counting()));
        
        // 获取分类信息
        Map<Integer, String> categoryNameMap = new HashMap<>();
        categoryCountMap.keySet().forEach(categoryId -> {
            DeviceCategory category = deviceCategoryMapper.selectById(categoryId);
            if (category != null) {
                // 根据分类名称判断是生物还是化学
                String labType = category.getName().contains("生物") ? "bio" : 
                                category.getName().contains("化学") ? "chem" : "other";
                categoryNameMap.put(categoryId, labType);
            }
        });
        
        // 统计各类型占比
        long bioCount = 0;
        long chemCount = 0;
        
        for (Map.Entry<Integer, Long> entry : categoryCountMap.entrySet()) {
            String labType = categoryNameMap.getOrDefault(entry.getKey(), "other");
            if ("bio".equals(labType)) {
                bioCount += entry.getValue();
            } else if ("chem".equals(labType)) {
                chemCount += entry.getValue();
            }
        }
        
        long totalCount = bioCount + chemCount;
        
        Map<String, Object> ratio = new HashMap<>();
        ratio.put("bio", totalCount > 0 ? (double) bioCount / totalCount : 0);
        ratio.put("chem", totalCount > 0 ? (double) chemCount / totalCount : 0);
        
        result.put("ratio", ratio);
        return result;
    }
    
    @Override
    public Map<String, Object> getMonthlyTrend(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 默认查询最近12个月
        LocalDate end = endDate != null && !endDate.isEmpty() 
                ? LocalDate.parse(endDate, DATE_FORMATTER) 
                : LocalDate.now();
        LocalDate start = startDate != null && !startDate.isEmpty() 
                ? LocalDate.parse(startDate, DATE_FORMATTER) 
                : end.minusMonths(11);
        
        wrapper.ge(BorrowRecord::getBorrowTime, LocalDateTime.of(start, java.time.LocalTime.MIN));
        wrapper.le(BorrowRecord::getBorrowTime, LocalDateTime.of(end, java.time.LocalTime.MAX));
        
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        // 按月份分组统计
        Map<String, Long> monthlyCountMap = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getBorrowTime().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));
        
        // 生成完整的月份列表（包含0借用的月份）
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            String month = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("count", monthlyCountMap.getOrDefault(month, 0L));
            trend.add(item);
            current = current.plusMonths(1);
        }
        
        result.put("trend", trend);
        return result;
    }
    
    @Override
    public Map<String, Object> getStudentActivity(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 时间范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(BorrowRecord::getBorrowTime, LocalDateTime.parse(startDate + " 00:00:00", DATETIME_FORMATTER));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(BorrowRecord::getBorrowTime, LocalDateTime.parse(endDate + " 23:59:59", DATETIME_FORMATTER));
        }
        
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        // 按学生ID分组统计
        Map<Integer, Long> studentCountMap = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getStudentId, Collectors.counting()));
        
        // 获取学生信息并排序
        List<Map<String, Object>> topStudents = studentCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10) // 取TOP10
                .map(entry -> {
                    Student student = studentMapper.selectById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", student != null ? student.getName() : "未知学生");
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        result.put("topStudents", topStudents);
        return result;
    }
    
    @Override
    public Map<String, Object> getViolationStats(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        LambdaQueryWrapper<Violation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Violation::getStatus, 1); // 只统计有效的违规记录
        
        // 时间范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Violation::getViolationTime, LocalDateTime.parse(startDate + " 00:00:00", DATETIME_FORMATTER));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Violation::getViolationTime, LocalDateTime.parse(endDate + " 23:59:59", DATETIME_FORMATTER));
        }
        
        List<Violation> violations = violationMapper.selectList(wrapper);
        
        // 按违规类型分组统计
        Map<String, Long> typeCountMap = violations.stream()
                .collect(Collectors.groupingBy(Violation::getType, Collectors.counting()));
        
        long totalCount = violations.size();
        
        Map<String, Object> ratio = new HashMap<>();
        ratio.put("overdue", totalCount > 0 ? (double) typeCountMap.getOrDefault("overdue", 0L) / totalCount : 0);
        ratio.put("damage", totalCount > 0 ? (double) typeCountMap.getOrDefault("damage", 0L) / totalCount : 0);
        ratio.put("other", totalCount > 0 ? (double) typeCountMap.getOrDefault("other", 0L) / totalCount : 0);
        
        result.put("typeRatio", ratio);
        return result;
    }
}
