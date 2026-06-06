package com.lab.backed.service;

import java.util.Map;

/**
 * 老师端报表服务接口
 */
public interface TeacherReportService {
    
    /**
     * 生成报表
     * @param reportType 报表类型（monthly/semester/yearly）
     * @param format 导出格式（excel/pdf）
     * @return 报表文件路径或下载URL
     */
    String generateReport(String reportType, String format);
}
