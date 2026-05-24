package com.lab.backed.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.entity.BorrowRecord;

/**
 * 借用记录服务接口
 */
public interface BorrowRecordService {
    
    /**
     * 获取学生的借用记录列表（分页）
     * @param studentId 学生ID
     * @param type 类型：current/history
     * @param page 页码
     * @param size 每页数量
     */
    Page<BorrowRecord> getStudentBorrows(Integer studentId, String type, Integer page, Integer size);
}
