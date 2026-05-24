package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.Reservation;
import com.lab.backed.entity.Student;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.ReservationMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 预约服务实现
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationMapper reservationMapper;
    private final DeviceMapper deviceMapper;
    private final StudentMapper studentMapper;
    
    @Override
    @Transactional
    public Integer createReservation(Reservation reservation) {
        // 检查设备是否存在且可预约
        Device device = deviceMapper.selectById(reservation.getDeviceId());
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        if (!"available".equals(device.getStatus())) {
            throw new RuntimeException("设备当前不可预约");
        }
        
        // 检查时间是否冲突
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getDeviceId, reservation.getDeviceId())
               .in(Reservation::getStatus, "pending", "approved")
               .and(w -> w.between(Reservation::getStartTime, reservation.getStartTime(), reservation.getEndTime())
                         .or()
                         .between(Reservation::getEndTime, reservation.getStartTime(), reservation.getEndTime())
                         .or()
                         .le(Reservation::getStartTime, reservation.getStartTime())
                         .ge(Reservation::getEndTime, reservation.getEndTime()));
        
        long count = reservationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该时段已被预约，请选择其他时间");
        }
        
        // 设置默认值
        reservation.setStatus("pending");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        
        reservationMapper.insert(reservation);
        return reservation.getId();
    }
    
    @Override
    public Page<Reservation> getStudentReservations(Integer studentId, String type, String status, Integer page, Integer size) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getStudentId, studentId);
        
        // 类型筛选
        if ("current".equals(type)) {
            // 当前预约：待审核、已通过、延期申请中
            wrapper.in(Reservation::getStatus, "pending", "approved", "extending");
        } else if ("history".equals(type)) {
            // 历史预约：已取消、被拒绝、已完成（借用后归还）
            wrapper.in(Reservation::getStatus, "cancelled", "rejected");
        }
        
        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(Reservation::getStatus, status);
        }
        
        wrapper.orderByDesc(Reservation::getCreatedAt);
        
        Page<Reservation> reservationPage = new Page<>(page, size);
        Page<Reservation> result = reservationMapper.selectPage(reservationPage, wrapper);
        
        // 填充设备和学生信息
        result.getRecords().forEach(this::fillReservationInfo);
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean cancelReservation(Integer reservationId, Integer studentId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        if (!reservation.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权操作此预约");
        }
        
        if (!"pending".equals(reservation.getStatus())) {
            throw new RuntimeException("只有待审核的预约可以取消");
        }
        
        reservation.setStatus("cancelled");
        reservation.setReason("用户主动取消");
        reservation.setUpdatedAt(LocalDateTime.now());
        
        return reservationMapper.updateById(reservation) > 0;
    }
    
    /**
     * 填充预约信息
     */
    private void fillReservationInfo(Reservation reservation) {
        // 填充设备信息
        if (reservation.getDeviceId() != null) {
            Device device = deviceMapper.selectById(reservation.getDeviceId());
            if (device != null) {
                reservation.setDeviceName(device.getName());
                reservation.setDeviceCode(device.getCode());
            }
        }
        
        // 填充学生信息
        if (reservation.getStudentId() != null) {
            Student student = studentMapper.selectById(reservation.getStudentId());
            if (student != null) {
                reservation.setStudentName(student.getName());
                reservation.setStudentNo(student.getStudentNo());
            }
        }
    }
}
