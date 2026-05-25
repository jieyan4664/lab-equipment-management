package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.Student;
import com.lab.backed.mapper.BorrowRecordMapper;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.service.TeacherBorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端借用归还管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherBorrowServiceImpl implements TeacherBorrowService {
    
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;
    private final StudentMapper studentMapper;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<Map<String, Object>> getCurrentBorrows(String keyword, Boolean isOverdue) {
        // 构建查询条件：只查询未归还的记录
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BorrowRecord::getStatus, "borrowed", "overdue")
               .orderByDesc(BorrowRecord::getBorrowTime);
        
        // 执行查询
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> borrowList = records.stream()
                .map(r -> {
                    Map<String, Object> rMap = new HashMap<>();
                    rMap.put("id", r.getId());
                    
                    // 获取设备信息
                    Device device = deviceMapper.selectById(r.getDeviceId());
                    rMap.put("deviceName", device != null ? device.getName() : "未知设备");
                    rMap.put("deviceCode", device != null ? device.getCode() : "未知编号");
                    
                    // 获取学生信息
                    Student student = studentMapper.selectById(r.getStudentId());
                    rMap.put("studentName", student != null ? student.getName() : "未知学生");
                    rMap.put("studentNo", student != null ? student.getStudentNo() : "未知学号");
                    
                    rMap.put("borrowTime", r.getBorrowTime().format(DATETIME_FORMATTER));
                    rMap.put("dueTime", r.getDueTime().format(DATETIME_FORMATTER));
                    
                    // 计算超时天数
                    LocalDateTime now = LocalDateTime.now();
                    long overdueDays = 0;
                    if (r.getDueTime().isBefore(now)) {
                        overdueDays = Duration.between(r.getDueTime(), now).toDays();
                    }
                    rMap.put("overdueDays", overdueDays);
                    
                    // 状态
                    String status = overdueDays > 0 ? "overdue" : "borrowed";
                    rMap.put("status", status);
                    
                    return rMap;
                })
                .collect(Collectors.toList());
        
        // 应用筛选
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase();
            borrowList = borrowList.stream()
                    .filter(r -> {
                        String deviceName = ((String) r.get("deviceName")).toLowerCase();
                        String studentName = ((String) r.get("studentName")).toLowerCase();
                        return deviceName.contains(kw) || studentName.contains(kw);
                    })
                    .collect(Collectors.toList());
        }
        
        if (isOverdue != null) {
            borrowList = borrowList.stream()
                    .filter(r -> {
                        long overdueDays = (Long) r.get("overdueDays");
                        return isOverdue ? overdueDays > 0 : overdueDays == 0;
                    })
                    .collect(Collectors.toList());
        }
        
        return borrowList;
    }
    
    @Override
    @Transactional
    public Map<String, Object> createBorrow(String deviceCode, String studentNo, 
                                           String dueTime, String remark, Integer teacherId) {
        // 根据设备编号查找设备
        LambdaQueryWrapper<Device> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.eq(Device::getCode, deviceCode);
        Device device = deviceMapper.selectOne(deviceWrapper);
        
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 检查设备状态
        if (!"available".equals(device.getStatus())) {
            throw new RuntimeException("设备当前不可借用（状态：" + device.getStatus() + "）");
        }
        
        // 根据学号查找学生
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getStudentNo, studentNo);
        Student student = studentMapper.selectOne(studentWrapper);
        
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 检查学生状态
        if (student.getAccessStatus() != null && student.getAccessStatus() == 2) {
            throw new RuntimeException("学生权限已被禁用");
        }
        
        // 解析应还时间
        LocalDateTime dueTimeDT;
        try {
            dueTimeDT = LocalDateTime.parse(dueTime, DATETIME_FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("应还时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }
        
        // 创建借用记录
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setDeviceId(device.getId());
        borrowRecord.setStudentId(student.getId());
        borrowRecord.setTeacherId(teacherId);
        borrowRecord.setBorrowTime(LocalDateTime.now());
        borrowRecord.setDueTime(dueTimeDT);
        borrowRecord.setStatus("borrowed");
        borrowRecord.setIsOverdue(0);
        borrowRecord.setRemark(remark);
        
        borrowRecordMapper.insert(borrowRecord);
        
        // 更新设备状态
        device.setStatus("borrowed");
        device.setCurrentBorrowerId(student.getId());
        device.setExpectedReturnTime(dueTimeDT);
        deviceMapper.updateById(device);
        
        // 生成归还凭证码
        String returnCode = "RET-" + String.format("%03d", device.getId()) + "-" + borrowRecord.getId();
        
        Map<String, Object> result = new HashMap<>();
        result.put("borrowId", borrowRecord.getId());
        result.put("returnCode", returnCode);
        
        return result;
    }
    
    @Override
    @Transactional
    public void returnBorrow(String deviceCode, String equipmentCondition, 
                            String violationType, String violationDescription, Integer teacherId) {
        // 根据设备编号查找设备
        LambdaQueryWrapper<Device> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.eq(Device::getCode, deviceCode);
        Device device = deviceMapper.selectOne(deviceWrapper);
        
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 查找该设备的当前借用记录
        LambdaQueryWrapper<BorrowRecord> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(BorrowRecord::getDeviceId, device.getId())
                    .in(BorrowRecord::getStatus, "borrowed", "overdue")
                    .orderByDesc(BorrowRecord::getBorrowTime)
                    .last("LIMIT 1");
        
        BorrowRecord borrowRecord = borrowRecordMapper.selectOne(borrowWrapper);
        
        if (borrowRecord == null) {
            throw new RuntimeException("该设备没有未归还的借用记录");
        }
        
        // 更新借用记录
        borrowRecord.setReturnTime(LocalDateTime.now());
        borrowRecord.setStatus("returned");
        borrowRecord.setEquipmentCondition(equipmentCondition);
        
        // 检查是否超时
        if (borrowRecord.getDueTime().isBefore(LocalDateTime.now())) {
            borrowRecord.setIsOverdue(1);
        }
        
        borrowRecordMapper.updateById(borrowRecord);
        
        // 更新设备状态
        device.setStatus("available");
        device.setCurrentBorrowerId(null);
        device.setExpectedReturnTime(null);
        deviceMapper.updateById(device);
        
        // TODO: 如果有违规，创建违规记录
        if (violationType != null && !"none".equals(violationType)) {
            System.out.println("创建违规记录 - 类型: " + violationType + ", 说明: " + violationDescription);
            // TODO: 调用ViolationService创建违规记录
        }
        
        System.out.println("归还登记完成 - 设备: " + deviceCode + ", 状态: " + equipmentCondition);
    }
    
    @Override
    public void remindReturn(Integer borrowId) {
        // 查找借用记录
        BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
        
        if (borrowRecord == null) {
            throw new RuntimeException("借用记录不存在");
        }
        
        // 获取学生信息
        Student student = studentMapper.selectById(borrowRecord.getStudentId());
        
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // TODO: 发送催还通知（站内信/短信）
        System.out.println("发送催还通知给学生: " + student.getName() + " (学号: " + student.getStudentNo() + ")");
        
        // TODO: 创建通知记录
    }
}
