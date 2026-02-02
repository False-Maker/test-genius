package com.sinosoft.testdesign.util;

import com.sinosoft.testdesign.dto.TaskDetailDTO;
import com.sinosoft.testdesign.entity.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * 用例导出工具类
 * 用于将用例生成任务导出为Excel文件
 *
 * @author sinosoft
 * @date 2025-01-30
 */
@Slf4j
public class CaseExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 将任务导出为Excel
     *
     * @param taskDetail 任务详情
     * @return Excel字节数组
     * @throws IOException IO异常
     */
    public static byte[] exportTaskToExcel(TaskDetailDTO taskDetail) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 创建工作表
            Sheet sheet = workbook.createSheet("用例生成任务");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);

            int rowNum = 0;

            // ===== 任务信息部分 =====
            // 标题行
            Row titleRow = sheet.createRow(rowNum++);
            createTitleCell(titleRow, 0, "任务信息", titleStyle);

            // 任务基本信息
            rowNum = writeTaskInfo(sheet, rowNum, taskDetail, normalStyle);
            rowNum += 1; // 空行

            // ===== 用例列表部分 =====
            // 标题行
            Row caseTitleRow = sheet.createRow(rowNum++);
            createTitleCell(caseTitleRow, 0, "用例列表", titleStyle);

            // 表头
            rowNum = writeCaseHeaders(sheet, rowNum, headerStyle);

            // 用例数据
            if (taskDetail.getCases() != null && !taskDetail.getCases().isEmpty()) {
                for (TestCase testCase : taskDetail.getCases()) {
                    rowNum = writeCaseRow(sheet, rowNum, testCase, normalStyle);
                }
            } else {
                // 如果没有用例，显示提示信息
                Row emptyRow = sheet.createRow(rowNum++);
                Cell emptyCell = emptyRow.createCell(0);
                emptyCell.setCellValue("暂无用例数据");
                emptyCell.setCellStyle(normalStyle);
            }

            // 自动调整列宽
            autoSizeColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new IOException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入任务信息
     */
    private static int writeTaskInfo(Sheet sheet, int startRow, TaskDetailDTO taskDetail, CellStyle style) {
        int rowNum = startRow;

        // 任务编号
        createInfoRow(sheet, rowNum++, "任务编号", taskDetail.getTaskCode(), style);
        // 需求编号
        createInfoRow(sheet, rowNum++, "需求编号", taskDetail.getRequirementCode(), style);
        // 需求名称
        createInfoRow(sheet, rowNum++, "需求名称", taskDetail.getRequirementName(), style);
        // 需求描述
        String description = taskDetail.getRequirementDescription();
        if (description != null && description.length() > 200) {
            description = description.substring(0, 200) + "...";
        }
        createInfoRow(sheet, rowNum++, "需求描述", description, style);
        // 生成时间
        String createTime = taskDetail.getCreateTime() != null
                ? taskDetail.getCreateTime().format(DATE_FORMATTER) : "-";
        createInfoRow(sheet, rowNum++, "生成时间", createTime, style);
        // 完成时间
        String completeTime = taskDetail.getCompleteTime() != null
                ? taskDetail.getCompleteTime().format(DATE_FORMATTER) : "-";
        createInfoRow(sheet, rowNum++, "完成时间", completeTime, style);
        // 测试分层
        createInfoRow(sheet, rowNum++, "测试分层",
                taskDetail.getLayerName() != null ? taskDetail.getLayerName() : "-", style);
        // 测试方法
        createInfoRow(sheet, rowNum++, "测试方法",
                taskDetail.getMethodName() != null ? taskDetail.getMethodName() : "-", style);
        // 模型
        createInfoRow(sheet, rowNum++, "模型", taskDetail.getModelCode(), style);
        // 任务状态
        createInfoRow(sheet, rowNum++, "任务状态", getTaskStatusText(taskDetail.getTaskStatus()), style);
        // 用例统计
        String caseStats = String.format("%d (成功: %d, 失败: %d)",
                taskDetail.getTotalCases(),
                taskDetail.getSuccessCases(),
                taskDetail.getFailCases());
        createInfoRow(sheet, rowNum++, "用例统计", caseStats, style);

        return rowNum;
    }

    /**
     * 写入用例表头
     */
    private static int writeCaseHeaders(Sheet sheet, int rowNum, CellStyle style) {
        Row headerRow = sheet.createRow(rowNum++);

        String[] headers = {
                "用例编码", "用例名称", "用例类型", "优先级", "状态",
                "前置条件", "测试步骤", "预期结果", "版本", "创建时间"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }

        return rowNum;
    }

    /**
     * 写入用例数据行
     */
    private static int writeCaseRow(Sheet sheet, int rowNum, TestCase testCase, CellStyle style) {
        Row row = sheet.createRow(rowNum++);

        createCell(row, 0, testCase.getCaseCode(), style);
        createCell(row, 1, testCase.getCaseName(), style);
        createCell(row, 2, testCase.getCaseType(), style);
        createCell(row, 3, testCase.getCasePriority(), style);
        createCell(row, 4, getCaseStatusText(testCase.getCaseStatus()), style);
        createCell(row, 5, testCase.getPreCondition(), style);
        createCell(row, 6, testCase.getTestStep(), style);
        createCell(row, 7, testCase.getExpectedResult(), style);
        createCell(row, 8, testCase.getVersion() != null ? String.valueOf(testCase.getVersion()) : "", style);

        String createTime = testCase.getCreateTime() != null
                ? testCase.getCreateTime().format(DATE_FORMATTER) : "-";
        createCell(row, 9, createTime, style);

        return rowNum;
    }

    /**
     * 创建信息行
     */
    private static void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value : "");
        valueCell.setCellStyle(style);
    }

    /**
     * 创建标题单元格
     */
    private static void createTitleCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * 创建普通单元格
     */
    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 自动调整列宽
     */
    private static void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 10; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小宽度和最大宽度
            int width = sheet.getColumnWidth(i);
            if (width < 2000) {
                sheet.setColumnWidth(i, 2000);
            } else if (width > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }

    /**
     * 创建标题样式
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * 创建普通样式
     */
    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * 获取任务状态文本
     */
    private static String getTaskStatusText(String taskStatus) {
        if (taskStatus == null) {
            return "-";
        }
        switch (taskStatus) {
            case "PENDING":
                return "待处理";
            case "PROCESSING":
                return "处理中";
            case "SUCCESS":
                return "成功";
            case "FAILED":
                return "失败";
            default:
                return taskStatus;
        }
    }

    /**
     * 获取用例状态文本
     */
    private static String getCaseStatusText(String caseStatus) {
        if (caseStatus == null) {
            return "-";
        }
        switch (caseStatus) {
            case "DRAFT":
                return "草稿";
            case "PENDING_REVIEW":
                return "待审核";
            case "REVIEWED":
                return "已审核";
            case "OBSOLETE":
                return "已废弃";
            default:
                return caseStatus;
        }
    }
}
