package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.backed.entity.Announcement;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Reservation;
import com.lab.backed.entity.Student;
import com.lab.backed.mapper.AnnouncementMapper;
import com.lab.backed.mapper.BorrowRecordMapper;
import com.lab.backed.mapper.ReservationMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生首页服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final StudentMapper studentMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final ReservationMapper reservationMapper;
    private final AnnouncementMapper announcementMapper;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public Map<String, Object> getStudentDashboard(Integer studentId) {
        // 1. 查询学生信息
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("id", student.getId());
        studentInfo.put("name", student.getName());
        studentInfo.put("studentNo", student.getStudentNo());
        studentInfo.put("labType", student.getLabType());
        studentInfo.put("accessExpire", student.getAccessExpire() != null 
            ? student.getAccessExpire().format(DATE_FORMATTER) : null);
        
        // 2. 统计当前借用设备数（status = 'borrowed' 或 'overdue'）
        LambdaQueryWrapper<BorrowRecord> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(BorrowRecord::getStudentId, studentId)
                     .in(BorrowRecord::getStatus, "borrowed", "overdue");
        long currentBorrowCount = borrowRecordMapper.selectCount(borrowWrapper);
        
        // 3. 统计待处理预约数（status = 'pending'）
        LambdaQueryWrapper<Reservation> reservationWrapper = new LambdaQueryWrapper<>();
        reservationWrapper.eq(Reservation::getStudentId, studentId)
                         .eq(Reservation::getStatus, "pending");
        long pendingReservationCount = reservationMapper.selectCount(reservationWrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("currentBorrowCount", currentBorrowCount);
        stats.put("pendingReservationCount", pendingReservationCount);
        
        // 4. 获取最新3条公告（根据实验室类型筛选）
        LambdaQueryWrapper<Announcement> announcementWrapper = new LambdaQueryWrapper<>();
        announcementWrapper.eq(Announcement::getStatus, 1)  // 正常状态
                          .and(w -> w.eq(Announcement::getTargetType, "all")  // 全部
                                   .or()
                                   .eq(Announcement::getTargetType, student.getLabType()))  // 对应实验室
                          .orderByDesc(Announcement::getIsPinned)  // 置顶优先
                          .orderByDesc(Announcement::getPublishTime)  // 时间倒序
                          .last("LIMIT 3");  // 只取3条
        
        List<Announcement> announcements = announcementMapper.selectList(announcementWrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> announcementList = announcements.stream().map(a -> {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("id", a.getId());
            aMap.put("title", a.getTitle());
            aMap.put("publishTime", a.getPublishTime().format(DATETIME_FORMATTER));
            aMap.put("isRead", false);  // TODO: 实际应该从通知表查询已读状态
            return aMap;
        }).collect(Collectors.toList());
        
        // 5. 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("student", studentInfo);
        result.put("stats", stats);
        result.put("announcements", announcementList);
        
        return result;
    }
}
