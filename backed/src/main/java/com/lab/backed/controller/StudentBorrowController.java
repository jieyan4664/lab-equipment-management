package com.lab.backed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.backed.common.Result;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端借用记录控制器
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentBorrowController {
    
    private final BorrowRecordService borrowRecordService;
    
    /**
     * 获取我的借用记录列表
     */
    @GetMapping("/borrows")
    public Result<Page<BorrowRecord>> getBorrows(
            @RequestParam(required = false, defaultValue = "current") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // TODO: 从Token中获取学生ID，暂时使用模拟数据
        Integer studentId = 1;
        
        Page<BorrowRecord> result = borrowRecordService.getStudentBorrows(studentId, type, page, size);
        return Result.success(result);
    }
}
