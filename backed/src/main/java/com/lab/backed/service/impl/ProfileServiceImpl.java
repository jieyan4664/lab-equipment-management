package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.Student;
import com.lab.backed.entity.Teacher;
import com.lab.backed.entity.Violation;
import com.lab.backed.mapper.BorrowRecordMapper;
import com.lab.backed.mapper.DeviceMapper;
import com.lab.backed.mapper.StudentMapper;
import com.lab.backed.mapper.TeacherMapper;
import com.lab.backed.mapper.ViolationMapper;
import com.lab.backed.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生个人中心服务实现
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    
    private final StudentMapper studentMapper;
    private final ViolationMapper violationMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final DeviceMapper deviceMapper;
    private final TeacherMapper teacherMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    
    @Override
    public Map<String, Object> getStudentProfile(Integer studentId) {
        // 查询学生信息
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 构建学生信息对象（前端期望的格式）
        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("name", student.getName());
        studentInfo.put("studentNo", student.getStudentNo());
        studentInfo.put("class", student.getClassName());
        studentInfo.put("phone", student.getPhone());
        studentInfo.put("email", student.getEmail());
        studentInfo.put("accessStatus", student.getAccessStatus() == 1 ? "normal" : "disabled");
        studentInfo.put("accessExpire", student.getAccessExpire() != null 
            ? student.getAccessExpire().format(DATE_FORMATTER) : null);
        
        // 查询违规记录
        LambdaQueryWrapper<Violation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Violation::getStudentId, studentId)
               .eq(Violation::getStatus, 1)  // 只查询有效的违规记录
               .orderByDesc(Violation::getViolationTime);
        
        List<Violation> violations = violationMapper.selectList(wrapper);
        
        // 转换违规记录为前端期望的格式
        List<Map<String, Object>> violationList = violations.stream().map(v -> {
            Map<String, Object> vMap = new HashMap<>();
            vMap.put("id", v.getId());
            vMap.put("time", v.getViolationTime().format(DATETIME_FORMATTER));
            vMap.put("deviceName", getDeviceNameByBorrowId(v.getBorrowId()));
            vMap.put("type", v.getType());
            vMap.put("punishment", v.getPunishment());
            vMap.put("teacherName", getTeacherName(v.getTeacherId()));
            return vMap;
        }).collect(Collectors.toList());
        
        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("student", studentInfo);
        result.put("violations", violationList);
        
        return result;
    }
    
    @Override
    public boolean updateStudentProfile(Integer studentId, Map<String, String> updates) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 更新允许的字段（phone和email）
        if (updates.containsKey("phone")) {
            student.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            student.setEmail(updates.get("email"));
        }
        
        int rows = studentMapper.updateById(student);
        return rows > 0;
    }
    
    /**
     * 根据借用记录ID获取设备名称
     */
    private String getDeviceNameByBorrowId(Integer borrowId) {
        if (borrowId == null) {
            return "未知设备";
        }
        
        // 查询借用记录
        BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
        if (borrowRecord == null || borrowRecord.getDeviceId() == null) {
            return "未知设备";
        }
        
        // 查询设备信息
        Device device = deviceMapper.selectById(borrowRecord.getDeviceId());
        return device != null ? device.getName() : "未知设备";
    }
    
    /**
     * 根据老师ID获取老师姓名
     */
    private String getTeacherName(Integer teacherId) {
        if (teacherId == null) {
            return "未知老师";
        }
        
        // 查询老师信息
        Teacher teacher = teacherMapper.selectById(teacherId);
        return teacher != null ? teacher.getName() : "未知老师";
    }
}
