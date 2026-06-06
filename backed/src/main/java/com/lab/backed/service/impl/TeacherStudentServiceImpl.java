package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Student;
import com.lab.backed.mapper.BorrowRecordMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.service.TeacherStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 老师端学生管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TeacherStudentServiceImpl implements TeacherStudentService {
    
    private final StudentMapper studentMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    
    @Override
    public Map<String, Object> getStudentList(String className, String keyword, Integer page, Integer size) {
        // 构建查询条件
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        // 班级筛选（模糊匹配）
        if (className != null && !className.trim().isEmpty()) {
            wrapper.like(Student::getClassName, className);
        }
        
        // 关键词筛选（姓名或学号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Student::getName, keyword)
                             .or()
                             .like(Student::getStudentNo, keyword));
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Student::getCreatedAt);
        
        // 分页查询
        Page<Student> studentPage = new Page<>(page, size);
        Page<Student> result = studentMapper.selectPage(studentPage, wrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> studentList = result.getRecords().stream()
                .map(s -> {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("id", s.getId());
                    sMap.put("name", s.getName());
                    sMap.put("studentNo", s.getStudentNo());
                    sMap.put("class", s.getClassName());
                    sMap.put("phone", s.getPhone());
                    sMap.put("email", s.getEmail());
                    
                    // 准入状态转换
                    sMap.put("accessStatus", s.getAccessStatus() == 1 ? "normal" : "disabled");
                    
                    // 统计当前借用数
                    long currentBorrowCount = borrowRecordMapper.selectCount(
                        new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getStudentId, s.getId())
                            .in(BorrowRecord::getStatus, "borrowed", "overdue")
                    );
                    sMap.put("currentBorrowCount", currentBorrowCount);
                    
                    // 统计累计借用次数
                    long totalBorrowCount = borrowRecordMapper.selectCount(
                        new LambdaQueryWrapper<BorrowRecord>()
                            .eq(BorrowRecord::getStudentId, s.getId())
                    );
                    sMap.put("totalBorrowCount", totalBorrowCount);
                    
                    // 违规次数
                    sMap.put("violationCount", s.getViolationCount());
                    
                    return sMap;
                })
                .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("list", studentList);
        
        return resultMap;
    }
    
    @Override
    @Transactional
    public void updateStudent(Integer id, String className, String phone, String email) {
        // 检查学生是否存在
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 更新信息
        student.setClassName(className);
        student.setPhone(phone);
        student.setEmail(email);
        student.setUpdatedAt(LocalDateTime.now());
        
        studentMapper.updateById(student);
    }
    
    @Override
    @Transactional
    public void updateAccessStatus(Integer id, Integer status, String reason, Integer banDays) {
        // 检查学生是否存在
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 验证状态值
        if (status != 1 && status != 2) {
            throw new RuntimeException("无效的状态值");
        }
        
        // 如果是禁用，必须有理由
        if (status == 2 && (reason == null || reason.trim().isEmpty())) {
            throw new RuntimeException("禁用学生必须填写原因");
        }
        
        // 更新状态
        student.setAccessStatus(status);
        
        // 如果禁用，设置禁用期限
        if (status == 2 && banDays != null && banDays > 0) {
            // TODO: 计算禁用截止日期
            // student.setAccessExpire(LocalDate.now().plusDays(banDays));
        } else if (status == 1) {
            // 启用时清除禁用期限
            // student.setAccessExpire(null);
        }
        
        student.setUpdatedAt(LocalDateTime.now());
        studentMapper.updateById(student);
        
        // TODO: 发送通知给学生
    }
}
