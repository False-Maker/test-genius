package com.sinosoft.testdesign.service.impl;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.repository.*;
import com.sinosoft.testdesign.service.TestReportService;
import com.sinosoft.testdesign.service.TestReportTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试报告生成服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestReportServiceImpl implements TestReportService {
    
    private final TestReportRepository reportRepository;
    private final TestReportTemplateRepository templateRepository;
    private final TestReportTemplateService templateService;
    private final TestExecutionTaskRepository taskRepository;
    private final TestExecutionRecordRepository recordRepository;
    private final RequirementRepository requirementRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 文件存储根目录
     */
    @Value("${app.upload.base-path:./uploads}")
    private String basePath;
    
    private static final String REPORT_CODE_PREFIX = "RPT";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public TestReport generateReport(TestReport report) {
        log.info("生成测试报告: {}", report.getReportName());
        
        // 数据验证
        validateReport(report);
        
        // 自动生成报告编码（如果未提供）
        if (!StringUtils.hasText(report.getReportCode())) {
            report.setReportCode(generateReportCode());
        } else {
            // 检查编码是否已存在
            if (reportRepository.findByReportCode(report.getReportCode()).isPresent()) {
                throw new BusinessException("报告编码已存在: " + report.getReportCode());
            }
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(report.getReportStatus())) {
            report.setReportStatus("DRAFT");
        }
        
        // 汇总测试执行结果
        String reportContent = summarizeExecutionResults(report.getRequirementId(), report.getExecutionTaskId());
        report.setReportContent(reportContent);
        
        // 生成报告摘要
        String reportSummary = generateReportSummary(reportContent);
        report.setReportSummary(reportSummary);
        
        log.info("生成报告成功，编码: {}", report.getReportCode());
        return reportRepository.save(report);
    }
    
    @Override
    public TestReport getReportByCode(String reportCode) {
        return reportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new BusinessException("报告不存在: " + reportCode));
    }
    
    @Override
    public TestReport getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报告不存在"));
    }
    
    @Override
    public Page<TestReport> getReportList(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }
    
    @Override
    public List<TestReport> getReportsByRequirementId(Long requirementId) {
        return reportRepository.findByRequirementId(requirementId);
    }
    
    @Override
    public List<TestReport> getReportsByExecutionTaskId(Long executionTaskId) {
        return reportRepository.findByExecutionTaskId(executionTaskId);
    }
    
    @Override
    @Transactional
    public TestReport updateReport(Long id, TestReport report) {
        log.info("更新测试报告: {}", id);
        
        TestReport existing = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报告不存在"));
        
        // 不允许修改报告编码
        if (StringUtils.hasText(report.getReportCode()) && 
            !report.getReportCode().equals(existing.getReportCode())) {
            throw new BusinessException("不允许修改报告编码");
        }
        
        // 如果已发布，不允许修改
        if ("PUBLISHED".equals(existing.getReportStatus())) {
            throw new BusinessException("已发布的报告不允许修改");
        }
        
        // 更新字段
        if (StringUtils.hasText(report.getReportName())) {
            existing.setReportName(report.getReportName());
        }
        if (StringUtils.hasText(report.getReportType())) {
            existing.setReportType(report.getReportType());
        }
        if (report.getTemplateId() != null) {
            existing.setTemplateId(report.getTemplateId());
        }
        if (report.getRequirementId() != null) {
            existing.setRequirementId(report.getRequirementId());
        }
        if (report.getExecutionTaskId() != null) {
            existing.setExecutionTaskId(report.getExecutionTaskId());
        }
        if (StringUtils.hasText(report.getReportContent())) {
            existing.setReportContent(report.getReportContent());
        }
        if (StringUtils.hasText(report.getReportSummary())) {
            existing.setReportSummary(report.getReportSummary());
        }
        if (StringUtils.hasText(report.getGenerateConfig())) {
            existing.setGenerateConfig(report.getGenerateConfig());
        }
        
        log.info("更新报告成功，编码: {}", existing.getReportCode());
        return reportRepository.save(existing);
    }
    
    @Override
    @Transactional
    public TestReport publishReport(Long id) {
        log.info("发布测试报告: {}", id);
        
        TestReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报告不存在"));
        
        if ("PUBLISHED".equals(report.getReportStatus())) {
            throw new BusinessException("报告已经发布");
        }
        
        report.setReportStatus("PUBLISHED");
        report.setPublishTime(LocalDateTime.now());
        
        log.info("发布报告成功，编码: {}", report.getReportCode());
        return reportRepository.save(report);
    }
    
    @Override
    @Transactional
    public void deleteReport(Long id) {
        log.info("删除测试报告: {}", id);
        
        TestReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报告不存在"));
        
        // 如果已发布，不允许删除
        if ("PUBLISHED".equals(report.getReportStatus())) {
            throw new BusinessException("已发布的报告不允许删除");
        }
        
        reportRepository.delete(report);
        log.info("删除报告成功，编码: {}", report.getReportCode());
    }
    
    @Override
    public String exportReport(String reportCode, String format) {
        log.info("导出测试报告: {}, 格式: {}", reportCode, format);
        
        TestReport report = getReportByCode(reportCode);
        
        // 验证导出格式
        if (!StringUtils.hasText(format)) {
            throw new BusinessException("导出格式不能为空");
        }
        if (!format.matches("WORD|PDF|EXCEL")) {
            throw new BusinessException("导出格式必须是 WORD/PDF/EXCEL 之一");
        }
        
        // 生成文件路径（按日期组织）
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = reportCode + "." + getFileExtension(format);
        String relativePath = "reports/" + datePath + "/" + fileName;
        String fullPath = Paths.get(basePath, relativePath).toString();
        String fileUrl = "/api/v1/files/" + relativePath.replace("\\", "/");
        
        // 根据格式调用不同的导出方法
        try {
            // 创建目录
            Path targetPath = Paths.get(fullPath);
            Files.createDirectories(targetPath.getParent());
            
            switch (format) {
                case "EXCEL":
                    exportToExcel(report, targetPath);
                    break;
                case "WORD":
                    exportToWord(report, targetPath);
                    break;
                case "PDF":
                    exportToPdf(report, targetPath);
                    break;
                default:
                    throw new BusinessException("不支持的导出格式: " + format);
            }
            
            // 更新报告的文件信息
            report.setFileUrl(fileUrl);
            report.setFileFormat(format);
            reportRepository.save(report);
            
            log.info("导出报告成功，文件URL: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("导出报告失败: {}", e.getMessage(), e);
            throw new BusinessException("导出报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出为Excel格式
     * 使用EasyExcel实现
     */
    private void exportToExcel(TestReport report, Path targetPath) throws Exception {
        log.info("导出报告为Excel格式: {}", targetPath);
        
        // 解析报告内容（JSON）
        Map<String, Object> reportData = parseReportContent(report.getReportContent());
        
        try (FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
            // 创建Excel数据列表
            List<Map<String, Object>> excelData = convertReportDataToExcelList(reportData);
            
            // 使用EasyExcel写入文件（使用Map方式）
            // 注意：这里简化实现，实际可以根据报告类型使用不同的DTO类
            EasyExcel.write(outputStream)
                    .sheet("测试报告")
                    .doWrite(excelData);
            
            log.info("Excel导出完成: {}", targetPath);
        }
    }
    
    /**
     * 导出为Word格式
     * 使用Apache POI实现
     */
    private void exportToWord(TestReport report, Path targetPath) throws Exception {
        log.info("导出报告为Word格式: {}", targetPath);
        
        // 解析报告内容（JSON）
        Map<String, Object> reportData = parseReportContent(report.getReportContent());
        
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建标题
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(report.getReportName());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            
            // 添加报告摘要
            if (StringUtils.hasText(report.getReportSummary())) {
                XWPFParagraph summaryPara = document.createParagraph();
                XWPFRun summaryRun = summaryPara.createRun();
                summaryRun.setText("报告摘要：" + report.getReportSummary());
                summaryRun.setFontSize(12);
            }
            
            // 添加报告内容
            addReportContentToWord(document, reportData);
            
            // 保存文件
            try (FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
                document.write(outputStream);
            }
            
            log.info("Word导出完成: {}", targetPath);
        }
    }
    
    /**
     * 导出为PDF格式
     * 使用iText7实现
     */
    private void exportToPdf(TestReport report, Path targetPath) throws Exception {
        log.info("导出报告为PDF格式: {}", targetPath);
        
        // 解析报告内容（JSON）
        Map<String, Object> reportData = parseReportContent(report.getReportContent());
        
        try (FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            
            // 设置文档属性
            pdfDoc.getDocumentInfo().setTitle(report.getReportName());
            pdfDoc.getDocumentInfo().setAuthor("测试设计助手系统");
            pdfDoc.getDocumentInfo().setCreator("测试设计助手系统");
            
            // 添加标题
            Paragraph title = new Paragraph(report.getReportName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            
            // 添加报告基本信息
            addReportBasicInfoToPdf(document, report);
            
            // 添加报告摘要
            if (StringUtils.hasText(report.getReportSummary())) {
                Paragraph summaryTitle = new Paragraph("报告摘要")
                        .setFontSize(14)
                        .setBold()
                        .setMarginTop(15)
                        .setMarginBottom(10);
                document.add(summaryTitle);
                
                Paragraph summary = new Paragraph(report.getReportSummary())
                        .setFontSize(12)
                        .setMarginBottom(15);
                document.add(summary);
            }
            
            // 添加报告内容
            addReportContentToPdf(document, reportData);
            
            document.close();
            log.info("PDF导出完成: {}", targetPath);
        } catch (Exception e) {
            log.error("PDF导出失败: {}", e.getMessage(), e);
            throw new BusinessException("PDF导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加报告基本信息到PDF
     */
    private void addReportBasicInfoToPdf(Document document, TestReport report) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(15);
        
        // 报告编码
        infoTable.addCell(createInfoCell("报告编码", true));
        infoTable.addCell(createInfoCell(report.getReportCode() != null ? report.getReportCode() : "", false));
        
        // 报告类型
        infoTable.addCell(createInfoCell("报告类型", true));
        infoTable.addCell(createInfoCell(report.getReportType() != null ? report.getReportType() : "", false));
        
        // 报告状态
        infoTable.addCell(createInfoCell("报告状态", true));
        infoTable.addCell(createInfoCell(report.getReportStatus() != null ? report.getReportStatus() : "", false));
        
        // 创建时间
        infoTable.addCell(createInfoCell("创建时间", true));
        infoTable.addCell(createInfoCell(
                report.getCreateTime() != null ? report.getCreateTime().toString() : "", false));
        
        // 创建人
        if (report.getCreatorName() != null) {
            infoTable.addCell(createInfoCell("创建人", true));
            infoTable.addCell(createInfoCell(report.getCreatorName(), false));
        }
        
        document.add(infoTable);
    }
    
    /**
     * 创建信息表格单元格
     */
    private Cell createInfoCell(String text, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(11));
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setBold();
        }
        return cell;
    }
    
    /**
     * 解析报告内容（JSON格式）
     */
    private Map<String, Object> parseReportContent(String reportContent) {
        if (!StringUtils.hasText(reportContent)) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(reportContent, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析报告内容失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 将报告数据转换为Excel列表格式
     */
    private List<Map<String, Object>> convertReportDataToExcelList(Map<String, Object> reportData) {
        List<Map<String, Object>> excelData = new ArrayList<>();
        
        // 添加基本信息
        Map<String, Object> basicInfo = new LinkedHashMap<>();
        basicInfo.put("项目", "测试报告");
        basicInfo.put("报告名称", reportData.getOrDefault("reportName", ""));
        basicInfo.put("汇总时间", reportData.getOrDefault("summaryTime", ""));
        excelData.add(basicInfo);
        
        // 添加统计信息
        if (reportData.containsKey("statistics")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) reportData.get("statistics");
            Map<String, Object> statsRow = new LinkedHashMap<>();
            statsRow.put("项目", "统计信息");
            statsRow.put("总记录数", statistics.getOrDefault("totalRecords", ""));
            statsRow.put("成功率", statistics.getOrDefault("successRate", ""));
            statsRow.put("平均耗时", statistics.getOrDefault("avgDuration", ""));
            excelData.add(statsRow);
        }
        
        // 添加执行记录详情
        if (reportData.containsKey("executionRecords")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) reportData.get("executionRecords");
            for (Map<String, Object> record : records) {
                Map<String, Object> recordRow = new LinkedHashMap<>();
                recordRow.put("项目", "执行记录");
                recordRow.put("记录编码", record.getOrDefault("recordCode", ""));
                recordRow.put("执行状态", record.getOrDefault("executionStatus", ""));
                recordRow.put("执行耗时", record.getOrDefault("executionDuration", ""));
                excelData.add(recordRow);
            }
        }
        
        return excelData;
    }
    
    /**
     * 将报告内容添加到Word文档
     */
    private void addReportContentToWord(XWPFDocument document, Map<String, Object> reportData) {
        // 添加统计信息
        if (reportData.containsKey("statistics")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) reportData.get("statistics");
            
            XWPFParagraph statsPara = document.createParagraph();
            XWPFRun statsRun = statsPara.createRun();
            statsRun.setText("统计信息：");
            statsRun.setBold(true);
            
            if (statistics.containsKey("totalRecords")) {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                run.setText("总执行记录数：" + statistics.get("totalRecords"));
            }
            if (statistics.containsKey("successRate")) {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                run.setText("成功率：" + statistics.get("successRate") + "%");
            }
        }
        
        // 添加执行记录详情
        if (reportData.containsKey("executionRecords")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) reportData.get("executionRecords");
            
            XWPFParagraph recordsPara = document.createParagraph();
            XWPFRun recordsRun = recordsPara.createRun();
            recordsRun.setText("执行记录详情：");
            recordsRun.setBold(true);
            
            for (Map<String, Object> record : records) {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                run.setText(String.format("记录编码：%s，执行状态：%s",
                        record.getOrDefault("recordCode", ""),
                        record.getOrDefault("executionStatus", "")));
            }
        }
    }
    
    /**
     * 将报告内容添加到PDF文档
     */
    private void addReportContentToPdf(Document document, Map<String, Object> reportData) {
        // 添加统计信息
        if (reportData.containsKey("statistics")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) reportData.get("statistics");
            
            Paragraph statsTitle = new Paragraph("统计信息")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(10);
            document.add(statsTitle);
            
            // 创建统计信息表格
            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);
            
            if (statistics.containsKey("totalRecords")) {
                statsTable.addCell(createInfoCell("总执行记录数", true));
                statsTable.addCell(createInfoCell(String.valueOf(statistics.get("totalRecords")), false));
            }
            if (statistics.containsKey("successRate")) {
                statsTable.addCell(createInfoCell("成功率", true));
                statsTable.addCell(createInfoCell(statistics.get("successRate") + "%", false));
            }
            if (statistics.containsKey("avgDuration")) {
                statsTable.addCell(createInfoCell("平均耗时(ms)", true));
                statsTable.addCell(createInfoCell(String.valueOf(statistics.get("avgDuration")), false));
            }
            if (statistics.containsKey("totalTasks")) {
                statsTable.addCell(createInfoCell("总任务数", true));
                statsTable.addCell(createInfoCell(String.valueOf(statistics.get("totalTasks")), false));
            }
            
            // 添加状态统计
            if (statistics.containsKey("statusCount")) {
                @SuppressWarnings("unchecked")
                Map<String, Long> statusCount = (Map<String, Long>) statistics.get("statusCount");
                if (statusCount != null && !statusCount.isEmpty()) {
                    statsTable.addCell(createInfoCell("状态统计", true));
                    StringBuilder statusText = new StringBuilder();
                    for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
                        if (statusText.length() > 0) {
                            statusText.append("，");
                        }
                        statusText.append(entry.getKey()).append("：").append(entry.getValue());
                    }
                    statsTable.addCell(createInfoCell(statusText.toString(), false));
                }
            }
            
            document.add(statsTable);
        }
        
        // 添加执行记录详情
        if (reportData.containsKey("executionRecords")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) reportData.get("executionRecords");
            
            if (records != null && !records.isEmpty()) {
                Paragraph recordsTitle = new Paragraph("执行记录详情")
                        .setFontSize(14)
                        .setBold()
                        .setMarginTop(15)
                        .setMarginBottom(10);
                document.add(recordsTitle);
                
                // 创建执行记录表格
                Table recordsTable = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f, 1.5f, 1.5f, 2}))
                        .useAllAvailableWidth()
                        .setMarginBottom(15);
                
                // 表头
                recordsTable.addHeaderCell(createInfoCell("记录编码", true));
                recordsTable.addHeaderCell(createInfoCell("执行状态", true));
                recordsTable.addHeaderCell(createInfoCell("执行类型", true));
                recordsTable.addHeaderCell(createInfoCell("执行耗时(ms)", true));
                recordsTable.addHeaderCell(createInfoCell("执行时间", true));
                
                // 数据行
                for (Map<String, Object> record : records) {
                    recordsTable.addCell(createInfoCell(
                            String.valueOf(record.getOrDefault("recordCode", "")), false));
                    recordsTable.addCell(createInfoCell(
                            String.valueOf(record.getOrDefault("executionStatus", "")), false));
                    recordsTable.addCell(createInfoCell(
                            String.valueOf(record.getOrDefault("executionType", "")), false));
                    recordsTable.addCell(createInfoCell(
                            record.get("executionDuration") != null ? 
                                    String.valueOf(record.get("executionDuration")) : "", false));
                    recordsTable.addCell(createInfoCell(
                            record.get("executionTime") != null ? 
                                    String.valueOf(record.get("executionTime")) : "", false));
                }
                
                document.add(recordsTable);
            }
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String format) {
        switch (format) {
            case "WORD":
                return "docx";
            case "PDF":
                return "pdf";
            case "EXCEL":
                return "xlsx";
            default:
                return "txt";
        }
    }
    
    @Override
    public String summarizeExecutionResults(Long requirementId, Long executionTaskId) {
        log.info("汇总测试执行结果，需求ID: {}, 执行任务ID: {}", requirementId, executionTaskId);
        
        Map<String, Object> summary = new HashMap<>();
        
        // 1. 基础统计信息
        Map<String, Object> statistics = new HashMap<>();
        
        // 根据执行任务ID汇总
        if (executionTaskId != null) {
            List<TestExecutionRecord> records = recordRepository.findByTaskId(executionTaskId);
            statistics.put("totalRecords", records.size());
            
            // 按状态统计
            Map<String, Long> statusCount = records.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getExecutionStatus() != null ? r.getExecutionStatus() : "UNKNOWN",
                            Collectors.counting()));
            statistics.put("statusCount", statusCount);
            
            // 成功率
            long successCount = statusCount.getOrDefault("SUCCESS", 0L);
            long totalCount = records.size();
            double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
            statistics.put("successRate", String.format("%.2f", successRate));
            
            // 平均执行时间
            double avgDuration = records.stream()
                    .filter(r -> r.getExecutionDuration() != null)
                    .mapToInt(TestExecutionRecord::getExecutionDuration)
                    .average()
                    .orElse(0.0);
            statistics.put("avgDuration", String.format("%.2f", avgDuration));
            
            // 执行记录详情
            List<Map<String, Object>> recordDetails = records.stream()
                    .map(this::convertRecordToMap)
                    .collect(Collectors.toList());
            summary.put("executionRecords", recordDetails);
        }
        
        // 根据需求ID汇总
        if (requirementId != null) {
            // 查询关联的执行任务
            List<TestExecutionTask> tasks = taskRepository.findByRequirementId(requirementId);
            statistics.put("totalTasks", tasks.size());
            
            // 汇总所有任务的执行记录
            List<TestExecutionRecord> allRecords = new ArrayList<>();
            for (TestExecutionTask task : tasks) {
                allRecords.addAll(recordRepository.findByTaskId(task.getId()));
            }
            
            if (!allRecords.isEmpty()) {
                Map<String, Long> allStatusCount = allRecords.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getExecutionStatus() != null ? r.getExecutionStatus() : "UNKNOWN",
                                Collectors.counting()));
                statistics.put("allStatusCount", allStatusCount);
                
                long allSuccessCount = allStatusCount.getOrDefault("SUCCESS", 0L);
                long allTotalCount = allRecords.size();
                double allSuccessRate = allTotalCount > 0 ? (double) allSuccessCount / allTotalCount * 100 : 0.0;
                statistics.put("allSuccessRate", String.format("%.2f", allSuccessRate));
            }
        }
        
        summary.put("statistics", statistics);
        summary.put("summaryTime", LocalDateTime.now().toString());
        
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            log.error("汇总结果序列化失败: {}", e.getMessage(), e);
            throw new BusinessException("汇总结果序列化失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成报告摘要
     */
    private String generateReportSummary(String reportContent) {
        if (!StringUtils.hasText(reportContent)) {
            return "报告摘要：暂无内容";
        }
        
        try {
            Map<String, Object> content = objectMapper.readValue(reportContent, Map.class);
            Map<String, Object> statistics = (Map<String, Object>) content.get("statistics");
            
            if (statistics != null) {
                StringBuilder summary = new StringBuilder();
                summary.append("测试执行结果摘要：");
                
                if (statistics.containsKey("totalRecords")) {
                    summary.append("总执行记录数 ").append(statistics.get("totalRecords")).append(" 条");
                }
                if (statistics.containsKey("successRate")) {
                    summary.append("，成功率 ").append(statistics.get("successRate")).append("%");
                }
                if (statistics.containsKey("totalTasks")) {
                    summary.append("，总任务数 ").append(statistics.get("totalTasks")).append(" 个");
                }
                
                return summary.toString();
            }
        } catch (Exception e) {
            log.warn("生成报告摘要失败: {}", e.getMessage());
        }
        
        return "报告摘要：测试执行结果汇总完成";
    }
    
    /**
     * 转换执行记录为Map
     */
    private Map<String, Object> convertRecordToMap(TestExecutionRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("recordCode", record.getRecordCode());
        map.put("caseId", record.getCaseId());
        map.put("executionType", record.getExecutionType());
        map.put("executionStatus", record.getExecutionStatus());
        map.put("executionDuration", record.getExecutionDuration());
        map.put("executionTime", record.getExecutionTime() != null ? record.getExecutionTime().toString() : null);
        map.put("finishTime", record.getFinishTime() != null ? record.getFinishTime().toString() : null);
        map.put("errorMessage", record.getErrorMessage());
        return map;
    }
    
    /**
     * 验证报告数据
     */
    private void validateReport(TestReport report) {
        if (report == null) {
            throw new BusinessException("报告信息不能为空");
        }
        
        // 验证报告名称
        if (!StringUtils.hasText(report.getReportName())) {
            throw new BusinessException("报告名称不能为空");
        }
        if (report.getReportName().length() > 500) {
            throw new BusinessException("报告名称长度不能超过500个字符");
        }
        
        // 验证报告类型
        if (!StringUtils.hasText(report.getReportType())) {
            throw new BusinessException("报告类型不能为空");
        }
        String reportType = report.getReportType();
        if (!reportType.matches("EXECUTION|COVERAGE|QUALITY|RISK")) {
            throw new BusinessException("报告类型必须是 EXECUTION/COVERAGE/QUALITY/RISK 之一");
        }
    }
    
    /**
     * 生成报告编码
     * 格式：RPT-YYYYMMDD-序号（如 RPT-20240117-001）
     */
    private String generateReportCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = REPORT_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天前缀的报告，避免全表扫描
        List<TestReport> todayReports = reportRepository.findByReportCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestReport report : todayReports) {
            String code = report.getReportCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                    log.warn("报告编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        return prefix + String.format("%03d", newSequence);
    }
}

