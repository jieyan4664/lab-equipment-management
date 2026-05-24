package com.lab.backed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.Reservation;

/**
 * 预约服务接口
 */
public interface ReservationService {
    
    /**
     * 创建预约
     */
    Integer createReservation(Reservation reservation);
    
    /**
     * 获取学生的预约列表（分页）
     * @param studentId 学生ID
     * @param type 类型：current/history
     * @param status 状态筛选
     * @param page 页码
     * @param size 每页数量
     */
    Page<Reservation> getStudentReservations(Integer studentId, String type, String status, Integer page, Integer size);
    
    /**
     * 取消预约
     */
    boolean cancelReservation(Integer reservationId, Integer studentId);
}
