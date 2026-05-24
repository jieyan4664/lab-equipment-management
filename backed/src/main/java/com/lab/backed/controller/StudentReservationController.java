package com.lab.backed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.Result;
import com.lab.backed.entity.Reservation;
import com.lab.backed.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端预约控制器
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentReservationController {
    
    private final ReservationService reservationService;
    
    /**
     * 提交预约申请
     */
    @PostMapping("/reservations")
    public Result<Integer> createReservation(@RequestBody Reservation reservation) {
        try {
            // TODO: 从Token中获取学生ID，暂时使用模拟数据
            reservation.setStudentId(1);
            
            Integer reservationId = reservationService.createReservation(reservation);
            return Result.success(reservationId);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * 获取我的预约列表
     */
    @GetMapping("/reservations")
    public Result<Page<Reservation>> getReservations(
            @RequestParam(required = false, defaultValue = "current") String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        Page<Reservation> result = reservationService.getStudentReservations(studentId, type, status, page, size);
        return Result.success(result);
    }
    
    /**
     * 取消预约
     */
    @PutMapping("/reservations/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Integer id) {
        try {
            // TODO: 从Token中获取学生ID，暂时使用模拟数据
            Integer studentId = 1;
            
            boolean success = reservationService.cancelReservation(id, studentId);
            if (success) {
                return Result.success();
            } else {
                return Result.error(400, "取消失败");
            }
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
