package com.lab.backed.controller;

import com.lab.backed.common.Result;
import com.lab.backed.service.TeacherReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 老师端报表控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/teacher/reports")
public class TeacherReportController {

    @Autowired
    private TeacherReportService reportService;

    /**
     * 生成报表
     * POST /api/v1/teacher/reports/generate
     * 
     * @param params 报表参数（reportType, format）
     * @return 报表文件信息
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateReport(@RequestBody Map<String, String> params) {
        try {
            String reportType = params.get("reportType");
            String format = params.get("format");

            // 参数验证
            if (reportType == null || reportType.isEmpty()) {
                return Result.error("请选择报表类型");
            }
            if (format == null || format.isEmpty()) {
                return Result.error("请选择导出格式");
            }

            // 生成报表
            String filePath = reportService.generateReport(reportType, format);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", new File(filePath).getName());
            result.put("downloadUrl", "/api/v1/teacher/reports/download?path=" + 
                      URLEncoder.encode(filePath, "UTF-8"));
            
            log.info("报表生成成功: {}", filePath);
            return Result.success(result);

        } catch (Exception e) {
            log.error("生成报表失败", e);
            return Result.error("生成报表失败: " + e.getMessage());
        }
    }

    /**
     * 下载报表文件
     * GET /api/v1/teacher/reports/download
     * 
     * @param path 文件路径
     * @return 文件流
     */
    @GetMapping("/download")
    public void downloadReport(@RequestParam String path, HttpServletResponse response) {
        try {
            File file = new File(path);
            
            if (!file.exists()) {
                response.setStatus(404);
                response.getWriter().write("文件不存在");
                return;
            }

            // 设置响应头
            String fileName = file.getName();
            String contentType;
            
            // 根据文件扩展名设置Content-Type
            if (fileName.endsWith(".xlsx")) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if (fileName.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else {
                contentType = "text/csv; charset=UTF-8";
            }
            
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", 
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLength((int) file.length());

            // 输出文件流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            log.info("报表下载成功: {}", fileName);

        } catch (Exception e) {
            log.error("下载报表失败", e);
            try {
                response.setStatus(500);
                response.getWriter().write("下载失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("响应错误失败", ex);
            }
        }
    }
}
