package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.Reservation;
import com.lab.backed.entity.Student;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.ReservationMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.service.TeacherReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端预约审核服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherReservationServiceImpl implements TeacherReservationService {
    
    private final ReservationMapper reservationMapper;
    private final StudentMapper studentMapper;
    private final DeviceMapper deviceMapper;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Page<Map<String, Object>> getReservationList(String status, String studentName, 
                                                        String deviceName, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Reservation::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Reservation::getCreatedAt);
        
        // 分页查询
        Page<Reservation> reservationPage = new Page<>(page, size);
        Page<Reservation> result = reservationMapper.selectPage(reservationPage, wrapper);
        
        // 转换为前端期望的格式，并应用筛选
        List<Map<String, Object>> reservationList = result.getRecords().stream()
                .map(r -> {
                    Map<String, Object> rMap = new HashMap<>();
                    rMap.put("id", r.getId());
                    
                    // 获取学生信息
                    Student student = studentMapper.selectById(r.getStudentId());
                    rMap.put("studentName", student != null ? student.getName() : "未知学生");
                    rMap.put("studentNo", student != null ? student.getStudentNo() : "未知学号");
                    
                    // 获取设备信息
                    Device device = deviceMapper.selectById(r.getDeviceId());
                    rMap.put("deviceName", device != null ? device.getName() : "未知设备");
                    rMap.put("deviceCode", device != null ? device.getCode() : "未知编号");
                    
                    rMap.put("startTime", r.getStartTime().format(DATETIME_FORMATTER));
                    rMap.put("endTime", r.getEndTime().format(DATETIME_FORMATTER));
                    rMap.put("purpose", r.getPurpose());
                    rMap.put("status", r.getStatus());
                    rMap.put("reason", r.getReason());
                    
                    // 计算等待时长（小时）
                    long waitingHours = Duration.between(r.getCreatedAt(), LocalDateTime.now()).toHours();
                    rMap.put("waitingHours", waitingHours);
                    
                    rMap.put("createdAt", r.getCreatedAt().format(DATETIME_FORMATTER));
                    
                    return rMap;
                })
                .collect(Collectors.toList());
        
        // 应用前端筛选（学生姓名和设备名称）
        if ((studentName != null && !studentName.trim().isEmpty()) || 
            (deviceName != null && !deviceName.trim().isEmpty())) {
            reservationList = reservationList.stream()
                    .filter(r -> {
                        boolean matchStudent = true;
                        boolean matchDevice = true;
                        
                        if (studentName != null && !studentName.trim().isEmpty()) {
                            matchStudent = ((String) r.get("studentName")).contains(studentName);
                        }
                        
                        if (deviceName != null && !deviceName.trim().isEmpty()) {
                            matchDevice = ((String) r.get("deviceName")).contains(deviceName);
                        }
                        
                        return matchStudent && matchDevice;
                    })
                    .collect(Collectors.toList());
        }
        
        // 构建返回的分页对象
        Page<Map<String, Object>> returnPage = new Page<>(page, size);
        returnPage.setTotal(reservationList.size());
        returnPage.setRecords(reservationList);
        
        return returnPage;
    }
    
    @Override
    @Transactional
    public void auditReservation(Integer id, String result, String reason, Integer teacherId) {
        // 检查预约是否存在
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 检查预约状态
        if (!"pending".equals(reservation.getStatus())) {
            throw new RuntimeException("该预约已审核，无法重复操作");
        }
        
        // 验证审核结果
        if (!"approve".equals(result) && !"reject".equals(result)) {
            throw new RuntimeException("无效的审核结果");
        }
        
        // 如果是拒绝，必须有理由
        if ("reject".equals(result) && (reason == null || reason.trim().isEmpty())) {
            throw new RuntimeException("拒绝预约必须填写理由");
        }
        
        // 更新预约状态
        reservation.setStatus("approve".equals(result) ? "approved" : "rejected");
        reservation.setReason(reason);
        reservation.setTeacherId(teacherId);
        reservation.setAuditTime(LocalDateTime.now());
        
        reservationMapper.updateById(reservation);
        
        // TODO: 发送通知给学生
        System.out.println("预约审核完成 - ID: " + id + ", 结果: " + result + ", 老师ID: " + teacherId);
    }
}
