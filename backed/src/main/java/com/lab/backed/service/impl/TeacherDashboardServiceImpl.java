package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.backed.entity.*;
import com.lab.backed.mapper.*;
import com.lab.backed.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端仪表盘服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherDashboardServiceImpl implements TeacherDashboardService {
    
    private final DeviceMapper deviceMapper;
    private final ReservationMapper reservationMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final StudentMapper studentMapper;
    private final ViolationMapper violationMapper;
    
    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 统计数据
        result.put("stats", getStats());
        
        // 2. 待办事项
        result.put("todos", getTodos());
        
        // 3. 图表数据
        result.put("charts", getCharts());
        
        return result;
    }
    
    /**
     * 获取统计数据
     */
    private Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 设备总数
        long deviceCount = deviceMapper.selectCount(null);
        stats.put("deviceCount", deviceCount);
        
        // 可借用数
        LambdaQueryWrapper<Device> availableWrapper = new LambdaQueryWrapper<>();
        availableWrapper.eq(Device::getStatus, "available");
        long availableCount = deviceMapper.selectCount(availableWrapper);
        stats.put("availableCount", availableCount);
        
        // 维修中数
        LambdaQueryWrapper<Device> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.eq(Device::getStatus, "repair");
        long repairCount = deviceMapper.selectCount(repairWrapper);
        stats.put("repairCount", repairCount);
        
        // 今日预约数
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        LambdaQueryWrapper<Reservation> todayReservationWrapper = new LambdaQueryWrapper<>();
        todayReservationWrapper.ge(Reservation::getStartTime, startOfDay)
                              .lt(Reservation::getStartTime, endOfDay);
        long todayReservationCount = reservationMapper.selectCount(todayReservationWrapper);
        stats.put("todayReservationCount", todayReservationCount);
        
        // 待审核数
        LambdaQueryWrapper<Reservation> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Reservation::getStatus, "pending");
        long pendingAuditCount = reservationMapper.selectCount(pendingWrapper);
        stats.put("pendingAuditCount", pendingAuditCount);
        
        // 借用中数（包括borrowed和overdue）
        LambdaQueryWrapper<BorrowRecord> borrowedWrapper = new LambdaQueryWrapper<>();
        borrowedWrapper.in(BorrowRecord::getStatus, "borrowed", "overdue");
        long borrowedCount = borrowRecordMapper.selectCount(borrowedWrapper);
        stats.put("borrowedCount", borrowedCount);
        
        // 超时数
        LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.eq(BorrowRecord::getStatus, "overdue");
        long overdueCount = borrowRecordMapper.selectCount(overdueWrapper);
        stats.put("overdueCount", overdueCount);
        
        // 活跃学生数（最近30天有借用的学生）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<BorrowRecord> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.ge(BorrowRecord::getBorrowTime, thirtyDaysAgo)
                    .select(BorrowRecord::getStudentId)
                    .groupBy(BorrowRecord::getStudentId);
        List<BorrowRecord> activeRecords = borrowRecordMapper.selectList(activeWrapper);
        long activeStudentCount = activeRecords.stream()
                .map(BorrowRecord::getStudentId)
                .distinct()
                .count();
        stats.put("activeStudentCount", activeStudentCount);
        
        // 违规学生数（有有效违规记录的学生）
        LambdaQueryWrapper<Violation> violationWrapper = new LambdaQueryWrapper<>();
        violationWrapper.eq(Violation::getStatus, 1)
                       .select(Violation::getStudentId)
                       .groupBy(Violation::getStudentId);
        List<Violation> violationRecords = violationMapper.selectList(violationWrapper);
        long violationStudentCount = violationRecords.stream()
                .map(Violation::getStudentId)
                .distinct()
                .count();
        stats.put("violationStudentCount", violationStudentCount);
        
        return stats;
    }
    
    /**
     * 获取待办事项
     */
    private List<Map<String, Object>> getTodos() {
        List<Map<String, Object>> todos = new ArrayList<>();
        
        // 1. 超时未还（高优先级）
        LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.eq(BorrowRecord::getStatus, "overdue")
                     .orderByDesc(BorrowRecord::getDueTime)
                     .last("LIMIT 3");
        List<BorrowRecord> overdueRecords = borrowRecordMapper.selectList(overdueWrapper);
        
        for (BorrowRecord record : overdueRecords) {
            Map<String, Object> todo = new HashMap<>();
            todo.put("id", record.getId());
            todo.put("type", "overdue");
            
            // 获取设备名称
            Device device = deviceMapper.selectById(record.getDeviceId());
            todo.put("deviceName", device != null ? device.getName() : "未知设备");
            
            // 获取学生姓名
            Student student = studentMapper.selectById(record.getStudentId());
            todo.put("studentName", student != null ? student.getName() : "未知学生");
            
            // 计算超时天数
            long overdueDays = java.time.Duration.between(record.getDueTime(), LocalDateTime.now()).toDays();
            todo.put("time", overdueDays + "天");
            todo.put("priority", "high");
            
            todos.add(todo);
        }
        
        // 2. 待审核预约（中优先级）
        LambdaQueryWrapper<Reservation> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Reservation::getStatus, "pending")
                     .orderByAsc(Reservation::getCreatedAt)
                     .last("LIMIT 3");
        List<Reservation> pendingReservations = reservationMapper.selectList(pendingWrapper);
        
        for (Reservation reservation : pendingReservations) {
            Map<String, Object> todo = new HashMap<>();
            todo.put("id", reservation.getId());
            todo.put("type", "reservation");
            
            // 获取设备名称
            Device device = deviceMapper.selectById(reservation.getDeviceId());
            todo.put("deviceName", device != null ? device.getName() : "未知设备");
            
            // 获取学生姓名
            Student student = studentMapper.selectById(reservation.getStudentId());
            todo.put("studentName", student != null ? student.getName() : "未知学生");
            
            // 计算等待时长（小时）
            long waitingHours = java.time.Duration.between(reservation.getCreatedAt(), LocalDateTime.now()).toHours();
            todo.put("time", waitingHours + "小时");
            todo.put("priority", "medium");
            
            todos.add(todo);
        }
        
        // 按优先级排序：high > medium > low
        todos.sort((a, b) -> {
            String priorityA = (String) a.get("priority");
            String priorityB = (String) b.get("priority");
            if ("high".equals(priorityA)) return -1;
            if ("high".equals(priorityB)) return 1;
            if ("medium".equals(priorityA)) return -1;
            if ("medium".equals(priorityB)) return 1;
            return 0;
        });
        
        return todos;
    }
    
    /**
     * 获取图表数据
     */
    private Map<String, Object> getCharts() {
        Map<String, Object> charts = new HashMap<>();
        
        // 1. 借用TOP5设备（最近30天）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<BorrowRecord> topDevicesWrapper = new LambdaQueryWrapper<>();
        topDevicesWrapper.ge(BorrowRecord::getBorrowTime, thirtyDaysAgo);
        List<BorrowRecord> borrowRecords = borrowRecordMapper.selectList(topDevicesWrapper);
        
        // 统计每个设备的借用次数
        Map<Integer, Long> deviceBorrowCount = borrowRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getDeviceId, Collectors.counting()));
        
        // 取TOP5
        List<Map<String, Object>> topDevices = deviceBorrowCount.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> deviceMap = new HashMap<>();
                    Device device = deviceMapper.selectById(entry.getKey());
                    deviceMap.put("name", device != null ? device.getName() : "未知设备");
                    deviceMap.put("count", entry.getValue());
                    return deviceMap;
                })
                .collect(Collectors.toList());
        
        charts.put("topDevices", topDevices);
        
        // 2. 月度借用趋势（最近6个月）
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            
            LocalDateTime monthStartDT = monthStart.atStartOfDay();
            LocalDateTime monthEndDT = monthEnd.atStartOfDay();
            
            LambdaQueryWrapper<BorrowRecord> monthWrapper = new LambdaQueryWrapper<>();
            monthWrapper.ge(BorrowRecord::getBorrowTime, monthStartDT)
                       .lt(BorrowRecord::getBorrowTime, monthEndDT);
            long count = borrowRecordMapper.selectCount(monthWrapper);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
            monthData.put("count", count);
            monthlyTrend.add(monthData);
        }
        
        charts.put("monthlyTrend", monthlyTrend);
        
        return charts;
    }
}
