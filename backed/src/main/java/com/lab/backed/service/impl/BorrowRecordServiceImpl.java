package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.Student;
import com.lab.backed.entity.Violation;
import com.lab.backed.mapper.BorrowRecordMapper;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.mapper.ViolationMapper;
import com.lab.backed.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 借用记录服务实现
 */
@Service
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService {
    
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;
    private final StudentMapper studentMapper;
    private final ViolationMapper violationMapper;
    
    @Override
    public Page<BorrowRecord> getStudentBorrows(Integer studentId, String type, Integer page, Integer size) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getStudentId, studentId);
        
        // 类型筛选
        if ("current".equals(type)) {
            // 当前借用：borrowed、overdue状态
            wrapper.in(BorrowRecord::getStatus, "borrowed", "overdue");
        } else if ("history".equals(type)) {
            // 历史借用：returned状态
            wrapper.eq(BorrowRecord::getStatus, "returned");
        }
        
        wrapper.orderByDesc(BorrowRecord::getBorrowTime);
        
        Page<BorrowRecord> borrowPage = new Page<>(page, size);
        Page<BorrowRecord> result = borrowRecordMapper.selectPage(borrowPage, wrapper);
        
        // 填充借用记录信息
        result.getRecords().forEach(this::fillBorrowInfo);
        
        return result;
    }
    
    /**
     * 填充借用记录信息
     */
    private void fillBorrowInfo(BorrowRecord borrow) {
        // 填充设备信息
        if (borrow.getDeviceId() != null) {
            Device device = deviceMapper.selectById(borrow.getDeviceId());
            if (device != null) {
                borrow.setDeviceName(device.getName());
                borrow.setDeviceCode(device.getCode());
            }
        }
        
        // 填充学生信息
        if (borrow.getStudentId() != null) {
            Student student = studentMapper.selectById(borrow.getStudentId());
            if (student != null) {
                borrow.setStudentName(student.getName());
                borrow.setStudentNo(student.getStudentNo());
            }
        }
        
        // 计算剩余天数或超时天数
        if ("borrowed".equals(borrow.getStatus()) || "overdue".equals(borrow.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueTime = borrow.getDueTime();
            
            if (now.isBefore(dueTime)) {
                // 未超时，计算剩余天数
                Duration duration = Duration.between(now, dueTime);
                long days = duration.toDays();
                borrow.setRemainingDays((int) days);
                borrow.setOverdueDays(0);
            } else {
                // 已超时，计算超时天数
                Duration duration = Duration.between(dueTime, now);
                long days = duration.toDays();
                borrow.setOverdueDays((int) days);
                borrow.setRemainingDays(0);
            }
        }
        
        // 生成归还凭证码（仅当前借用）
        if ("borrowed".equals(borrow.getStatus()) || "overdue".equals(borrow.getStatus())) {
            // 格式：RET-设备编号-借用记录ID
            String returnCode = "RET-" + borrow.getDeviceCode() + "-" + borrow.getId();
            borrow.setReturnCode(returnCode);
        }
        
        // 查询关联的违规记录
        if (borrow.getIsOverdue() != null && borrow.getIsOverdue() == 1) {
            LambdaQueryWrapper<Violation> violationWrapper = new LambdaQueryWrapper<>();
            violationWrapper.eq(Violation::getBorrowId, borrow.getId())
                           .eq(Violation::getStudentId, borrow.getStudentId())
                           .eq(Violation::getStatus, 1)
                           .orderByDesc(Violation::getCreatedAt)
                           .last("LIMIT 1");
            
            Violation violation = violationMapper.selectOne(violationWrapper);
            borrow.setViolation(violation);
        }
    }
}
