package com.lab.backed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.backed.entity.BorrowRecord;
import com.lab.backed.entity.Device;
import com.lab.backed.entity.Student;
import com.lab.backed.entity.Violation;
import com.lab.backed.mapper.*;
import com.lab.backed.service.TeacherReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 老师端报表服务实现
 */
@Slf4j
@Service
public class TeacherReportServiceImpl implements TeacherReportService {

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ViolationMapper violationMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String generateReport(String reportType, String format) {
        try {
            // 计算时间范围
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;
            
            switch (reportType) {
                case "monthly":
                    startDate = endDate.minusMonths(1);
                    break;
                case "semester":
                    startDate = endDate.minusMonths(6);
                    break;
                case "yearly":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的报表类型: " + reportType);
            }

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileExtension;
            String fileName;
            
            // 根据格式确定文件扩展名
            if ("excel".equals(format)) {
                fileExtension = "xlsx";
            } else if ("pdf".equals(format)) {
                // PDF暂时使用HTML格式
                fileExtension = "html";
                log.warn("PDF格式暂未实现，使用HTML格式代替");
            } else {
                fileExtension = "csv";
            }
            
            fileName = String.format("report_%s_%s.%s", reportType, timestamp, fileExtension);
            
            // 创建临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            Path filePath = Paths.get(tempDir, fileName);

            // 生成报表内容
            if ("excel".equals(format)) {
                generateExcelReport(filePath, startDate, endDate, reportType);
            } else if ("pdf".equals(format)) {
                generateHTMLReport(filePath, startDate, endDate, reportType);
            } else {
                generateCSVReport(filePath, startDate, endDate, reportType);
            }

            log.info("报表生成成功: {}", filePath.toString());
            return filePath.toString();

        } catch (Exception e) {
            log.error("生成报表失败", e);
            throw new RuntimeException("生成报表失败: " + e.getMessage());
        }
    }

    /**
     * 生成CSV格式报表
     */
    private void generateCSVReport(Path filePath, LocalDate startDate, LocalDate endDate, String reportType) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // 写入标题
            writer.append("实验室设备管理统计报表\n");
            writer.append(String.format("报表类型: %s\n", getReportTypeName(reportType)));
            writer.append(String.format("统计周期: %s 至 %s\n", startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER)));
            writer.append(String.format("生成时间: %s\n\n", LocalDateTime.now().format(DATETIME_FORMATTER)));

            // 1. 设备借用统计
            writer.append("=== 设备借用统计 ===\n");
            writeDeviceBorrowStats(writer, startDate, endDate);
            writer.append("\n");

            // 2. 学生活跃度统计
            writer.append("=== 学生活跃度统计 ===\n");
            writeStudentActivityStats(writer, startDate, endDate);
            writer.append("\n");

            // 3. 违规统计
            writer.append("=== 违规统计 ===\n");
            writeViolationStats(writer, startDate, endDate);
            writer.append("\n");

            // 4. 设备状态统计
            writer.append("=== 设备状态统计 ===\n");
            writeDeviceStatusStats(writer);
            writer.append("\n");
        }
    }

    /**
     * 写入设备借用统计
     */
    private void writeDeviceBorrowStats(FileWriter writer, LocalDate startDate, LocalDate endDate) throws IOException {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(BorrowRecord::getBorrowTime, startDate.atStartOfDay())
               .le(BorrowRecord::getBorrowTime, endDate.atTime(23, 59, 59));
        
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        // 总借用次数
        writer.append(String.format("总借用次数: %d\n", records.size()));
        
        // 按设备分组统计
        Map<Integer, Long> deviceCountMap = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getDeviceId, Collectors.counting()));
        
        writer.append("TOP10设备借用排行:\n");
        writer.append("设备名称,借用次数\n");
        
        deviceCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Device device = deviceMapper.selectById(entry.getKey());
                    try {
                        writer.append(String.format("%s,%d\n", 
                                device != null ? device.getName() : "未知设备", 
                                entry.getValue()));
                    } catch (IOException e) {
                        log.error("写入数据失败", e);
                    }
                });
    }

    /**
     * 写入学生活跃度统计
     */
    private void writeStudentActivityStats(FileWriter writer, LocalDate startDate, LocalDate endDate) throws IOException {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(BorrowRecord::getBorrowTime, startDate.atStartOfDay())
               .le(BorrowRecord::getBorrowTime, endDate.atTime(23, 59, 59));
        
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        // 按学生分组统计
        Map<Integer, Long> studentCountMap = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getStudentId, Collectors.counting()));
        
        writer.append("TOP10活跃学生:\n");
        writer.append("学生姓名,学号,借用次数\n");
        
        studentCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Student student = studentMapper.selectById(entry.getKey());
                    try {
                        writer.append(String.format("%s,%s,%d\n", 
                                student != null ? student.getName() : "未知学生",
                                student != null ? student.getStudentNo() : "N/A",
                                entry.getValue()));
                    } catch (IOException e) {
                        log.error("写入数据失败", e);
                    }
                });
    }

    /**
     * 写入违规统计
     */
    private void writeViolationStats(FileWriter writer, LocalDate startDate, LocalDate endDate) throws IOException {
        LambdaQueryWrapper<Violation> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Violation::getViolationTime, startDate.atStartOfDay())
               .le(Violation::getViolationTime, endDate.atTime(23, 59, 59))
               .eq(Violation::getStatus, 1); // 只统计有效记录
        
        List<Violation> violations = violationMapper.selectList(wrapper);
        
        writer.append(String.format("违规总数: %d\n", violations.size()));
        
        // 按类型分组
        Map<String, Long> typeCountMap = violations.stream()
                .collect(Collectors.groupingBy(Violation::getType, Collectors.counting()));
        
        writer.append("违规类型分布:\n");
        writer.append("违规类型,次数\n");
        
        typeCountMap.forEach((type, count) -> {
            try {
                String typeName = getViolationTypeName(type);
                writer.append(String.format("%s,%d\n", typeName, count));
            } catch (IOException e) {
                log.error("写入数据失败", e);
            }
        });
    }

    /**
     * 写入设备状态统计
     */
    private void writeDeviceStatusStats(FileWriter writer) throws IOException {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        List<Device> devices = deviceMapper.selectList(wrapper);
        
        // 按状态分组（Device.status是String类型）
        Map<String, Long> statusCountMap = devices.stream()
                .collect(Collectors.groupingBy(
                    device -> String.valueOf(device.getStatus()), 
                    Collectors.counting()
                ));
        
        writer.append("设备总数: " + devices.size() + "\n");
        writer.append("设备状态分布:\n");
        writer.append("状态,数量\n");
        
        statusCountMap.forEach((status, count) -> {
            try {
                String statusName = getStatusName(status);
                writer.append(String.format("%s,%d\n", statusName, count));
            } catch (IOException e) {
                log.error("写入数据失败", e);
            }
        });
    }

    /**
     * 获取报表类型名称
     */
    private String getReportTypeName(String reportType) {
        switch (reportType) {
            case "monthly": return "月报";
            case "semester": return "学期报";
            case "yearly": return "年报";
            default: return reportType;
        }
    }

    /**
     * 获取违规类型名称
     */
    private String getViolationTypeName(String type) {
        switch (type) {
            case "overdue": return "超时未还";
            case "damage": return "设备损坏";
            case "loss": return "设备丢失";
            case "other": return "其他";
            default: return type;
        }
    }

    /**
     * 获取设备状态名称
     */
    private String getStatusName(String status) {
        switch (status) {
            case "0": return "正常";
            case "1": return "维修中";
            case "2": return "报废";
            default: return "未知";
        }
    }

    /**
     * 生成Excel格式报表
     */
    private void generateExcelReport(Path filePath, LocalDate startDate, LocalDate endDate, String reportType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Sheet 1: 设备借用统计
            Sheet deviceSheet = workbook.createSheet("设备借用统计");
            int rowNum = 0;
            
            // 标题行
            Row titleRow = deviceSheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("实验室设备管理统计报表 - " + getReportTypeName(reportType));
            titleCell.setCellStyle(headerStyle);
            deviceSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));
            
            rowNum++; // 空行
            
            // 统计周期
            Row periodRow = deviceSheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue("统计周期:");
            periodRow.createCell(1).setCellValue(startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER));
            
            // 总借用次数
            LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(BorrowRecord::getBorrowTime, startDate.atStartOfDay())
                   .le(BorrowRecord::getBorrowTime, endDate.atTime(23, 59, 59));
            List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
            
            Row totalRow = deviceSheet.createRow(rowNum++);
            totalRow.createCell(0).setCellValue("总借用次数:");
            totalRow.createCell(1).setCellValue(records.size());
            
            rowNum++; // 空行
            
            // TOP10设备排行表头
            Row headerRow = deviceSheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("排名");
            headerRow.createCell(1).setCellValue("设备名称");
            headerRow.createCell(2).setCellValue("借用次数");
            
            // 按设备分组统计
            Map<Integer, Long> deviceCountMap = records.stream()
                    .collect(Collectors.groupingBy(BorrowRecord::getDeviceId, Collectors.counting()));
            
            int rank = 1;
            for (Map.Entry<Integer, Long> entry : deviceCountMap.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList())) {
                Device device = deviceMapper.selectById(entry.getKey());
                Row dataRow = deviceSheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(rank++);
                dataRow.createCell(1).setCellValue(device != null ? device.getName() : "未知设备");
                dataRow.createCell(2).setCellValue(entry.getValue());
            }
            
            // 自动调整列宽
            for (int i = 0; i < 3; i++) {
                deviceSheet.autoSizeColumn(i);
            }

            // Sheet 2: 学生活跃度统计
            Sheet studentSheet = workbook.createSheet("学生活跃度统计");
            rowNum = 0;
            
            Row studentTitleRow = studentSheet.createRow(rowNum++);
            Cell studentTitleCell = studentTitleRow.createCell(0);
            studentTitleCell.setCellValue("学生活跃度TOP10");
            studentTitleCell.setCellStyle(headerStyle);
            
            rowNum++;
            
            Row studentHeaderRow = studentSheet.createRow(rowNum++);
            studentHeaderRow.createCell(0).setCellValue("排名");
            studentHeaderRow.createCell(1).setCellValue("学生姓名");
            studentHeaderRow.createCell(2).setCellValue("学号");
            studentHeaderRow.createCell(3).setCellValue("借用次数");
            
            Map<Integer, Long> studentCountMap = records.stream()
                    .collect(Collectors.groupingBy(BorrowRecord::getStudentId, Collectors.counting()));
            
            rank = 1;
            for (Map.Entry<Integer, Long> entry : studentCountMap.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList())) {
                Student student = studentMapper.selectById(entry.getKey());
                Row dataRow = studentSheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(rank++);
                dataRow.createCell(1).setCellValue(student != null ? student.getName() : "未知学生");
                dataRow.createCell(2).setCellValue(student != null ? student.getStudentNo() : "N/A");
                dataRow.createCell(3).setCellValue(entry.getValue());
            }
            
            for (int i = 0; i < 4; i++) {
                studentSheet.autoSizeColumn(i);
            }

            // Sheet 3: 违规统计
            Sheet violationSheet = workbook.createSheet("违规统计");
            rowNum = 0;
            
            Row violationTitleRow = violationSheet.createRow(rowNum++);
            Cell violationTitleCell = violationTitleRow.createCell(0);
            violationTitleCell.setCellValue("违规统计分析");
            violationTitleCell.setCellStyle(headerStyle);
            
            rowNum++;
            
            LambdaQueryWrapper<Violation> vWrapper = new LambdaQueryWrapper<>();
            vWrapper.ge(Violation::getViolationTime, startDate.atStartOfDay())
                    .le(Violation::getViolationTime, endDate.atTime(23, 59, 59))
                    .eq(Violation::getStatus, 1);
            List<Violation> violations = violationMapper.selectList(vWrapper);
            
            Row violationTotalRow = violationSheet.createRow(rowNum++);
            violationTotalRow.createCell(0).setCellValue("违规总数:");
            violationTotalRow.createCell(1).setCellValue(violations.size());
            
            rowNum++;
            
            Row violationHeaderRow = violationSheet.createRow(rowNum++);
            violationHeaderRow.createCell(0).setCellValue("违规类型");
            violationHeaderRow.createCell(1).setCellValue("次数");
            violationHeaderRow.createCell(2).setCellValue("占比");
            
            Map<String, Long> typeCountMap = violations.stream()
                    .collect(Collectors.groupingBy(Violation::getType, Collectors.counting()));
            
            for (Map.Entry<String, Long> entry : typeCountMap.entrySet()) {
                Row dataRow = violationSheet.createRow(rowNum++);
                String typeName = getViolationTypeName(entry.getKey());
                double percentage = violations.size() > 0 ? (entry.getValue() * 100.0 / violations.size()) : 0;
                dataRow.createCell(0).setCellValue(typeName);
                dataRow.createCell(1).setCellValue(entry.getValue());
                dataRow.createCell(2).setCellValue(String.format("%.2f%%", percentage));
            }
            
            for (int i = 0; i < 3; i++) {
                violationSheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }
    }

    /**
     * 生成HTML格式报表（PDF替代方案）
     */
    private void generateHTMLReport(Path filePath, LocalDate startDate, LocalDate endDate, String reportType) throws IOException {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='zh-CN'>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>实验室设备管理统计报表</title>\n");
        html.append("<style>");
        html.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 40px; }");
        html.append("h1 { color: #333; border-bottom: 3px solid #409EFF; padding-bottom: 10px; }");
        html.append("h2 { color: #666; margin-top: 30px; }");
        html.append(".info { background: #f5f7fa; padding: 15px; margin: 20px 0; border-left: 4px solid #409EFF; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th { background: #409EFF; color: white; padding: 12px; text-align: left; }");
        html.append("td { padding: 10px; border-bottom: 1px solid #ddd; }");
        html.append("tr:hover { background: #f5f7fa; }");
        html.append(".footer { margin-top: 40px; text-align: center; color: #999; font-size: 12px; }");
        html.append("</style>\n</head>\n<body>\n");
        
        // 标题
        html.append("<h1>实验室设备管理统计报表</h1>\n");
        
        // 基本信息
        html.append("<div class='info'>\n");
        html.append("<p><strong>报表类型：</strong>").append(getReportTypeName(reportType)).append("</p>\n");
        html.append("<p><strong>统计周期：</strong>").append(startDate.format(DATE_FORMATTER))
            .append(" 至 ").append(endDate.format(DATE_FORMATTER)).append("</p>\n");
        html.append("<p><strong>生成时间：</strong>").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("</p>\n");
        html.append("</div>\n");
        
        // 设备借用统计
        html.append("<h2>一、设备借用统计</h2>\n");
        
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(BorrowRecord::getBorrowTime, startDate.atStartOfDay())
               .le(BorrowRecord::getBorrowTime, endDate.atTime(23, 59, 59));
        List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);
        
        html.append("<p><strong>总借用次数：</strong>").append(records.size()).append("</p>\n");
        
        html.append("<h3>TOP10设备借用排行</h3>\n");
        html.append("<table>\n<tr><th>排名</th><th>设备名称</th><th>借用次数</th></tr>\n");
        
        Map<Integer, Long> deviceCountMap = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getDeviceId, Collectors.counting()));
        
        int rank = 1;
        for (Map.Entry<Integer, Long> entry : deviceCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList())) {
            Device device = deviceMapper.selectById(entry.getKey());
            html.append("<tr><td>").append(rank++).append("</td><td>")
                .append(device != null ? device.getName() : "未知设备").append("</td><td>")
                .append(entry.getValue()).append("</td></tr>\n");
        }
        html.append("</table>\n");
        
        // 学生活跃度统计
        html.append("<h2>二、学生活跃度统计</h2>\n");
        html.append("<h3>TOP10活跃学生</h3>\n");
        html.append("<table>\n<tr><th>排名</th><th>学生姓名</th><th>学号</th><th>借用次数</th></tr>\n");
        
        Map<Integer, Long> studentCountMap = records.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getStudentId, Collectors.counting()));
        
        rank = 1;
        for (Map.Entry<Integer, Long> entry : studentCountMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList())) {
            Student student = studentMapper.selectById(entry.getKey());
            html.append("<tr><td>").append(rank++).append("</td><td>")
                .append(student != null ? student.getName() : "未知学生").append("</td><td>")
                .append(student != null ? student.getStudentNo() : "N/A").append("</td><td>")
                .append(entry.getValue()).append("</td></tr>\n");
        }
        html.append("</table>\n");
        
        // 违规统计
        html.append("<h2>三、违规统计</h2>\n");
        
        LambdaQueryWrapper<Violation> vWrapper = new LambdaQueryWrapper<>();
        vWrapper.ge(Violation::getViolationTime, startDate.atStartOfDay())
                .le(Violation::getViolationTime, endDate.atTime(23, 59, 59))
                .eq(Violation::getStatus, 1);
        List<Violation> violations = violationMapper.selectList(vWrapper);
        
        html.append("<p><strong>违规总数：</strong>").append(violations.size()).append("</p>\n");
        
        html.append("<h3>违规类型分布</h3>\n");
        html.append("<table>\n<tr><th>违规类型</th><th>次数</th><th>占比</th></tr>\n");
        
        Map<String, Long> typeCountMap = violations.stream()
                .collect(Collectors.groupingBy(Violation::getType, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : typeCountMap.entrySet()) {
            String typeName = getViolationTypeName(entry.getKey());
            double percentage = violations.size() > 0 ? (entry.getValue() * 100.0 / violations.size()) : 0;
            html.append("<tr><td>").append(typeName).append("</td><td>")
                .append(entry.getValue()).append("</td><td>")
                .append(String.format("%.2f%%", percentage)).append("</td></tr>\n");
        }
        html.append("</table>\n");
        
        // 页脚
        html.append("<div class='footer'>\n");
        html.append("<p>本报告由实验室设备管理系统自动生成</p>\n");
        html.append("<p>生成时间：").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("</p>\n");
        html.append("</div>\n");
        
        html.append("</body>\n</html>");
        
        // 写入文件
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(html.toString());
        }
    }
}
