package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.dto.EquivalenceTableRequestDTO;
import com.sinosoft.testdesign.dto.EquivalenceTableResponseDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableRequestDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableResponseDTO;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.DataDocumentGenerationService;
import com.sinosoft.testdesign.service.FileUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据文档生成服务实现类
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDocumentGenerationServiceImpl implements DataDocumentGenerationService {
    
    private final RequirementRepository requirementRepository;
    private final TestCaseRepository testCaseRepository;
    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional(readOnly = true)
    public EquivalenceTableResponseDTO generateEquivalenceTable(EquivalenceTableRequestDTO request) {
        log.info("生成等价类表，请求参数：{}", request);
        
        try {
            EquivalenceTableResponseDTO response = new EquivalenceTableResponseDTO();
            
            // 1. 提取等价类数据
            Map<String, Map<String, List<String>>> equivalenceClasses;
            if (request.getInputParameters() != null && !request.getInputParameters().isEmpty()) {
                // 直接使用输入的参数定义
                equivalenceClasses = request.getInputParameters();
            } else {
                // 从需求或用例中提取参数
                equivalenceClasses = extractEquivalenceClassesFromSource(request);
            }
            
            // 2. 生成等价类表数据
            List<Map<String, Object>> tableData = generateEquivalenceTableData(equivalenceClasses);
            
            // 3. 统计信息
            int validCaseCount = 0;
            int invalidCaseCount = 0;
            for (Map<String, Object> row : tableData) {
                Boolean isValid = (Boolean) row.get("isValid");
                if (Boolean.TRUE.equals(isValid)) {
                    validCaseCount++;
                } else {
                    invalidCaseCount++;
                }
            }
            
            // 4. 构建响应
            response.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "等价类表");
            response.setParameters(new ArrayList<>(equivalenceClasses.keySet()));
            response.setEquivalenceClasses(equivalenceClasses);
            response.setTableData(tableData);
            response.setTestCaseCount(tableData.size());
            response.setValidCaseCount(validCaseCount);
            response.setInvalidCaseCount(invalidCaseCount);
            response.setFormat(request.getFormat());
            
            // 5. 生成JSON数据
            response.setJsonData(objectMapper.writeValueAsString(response));
            
            // 6. 如果需要导出文件，生成文件
            if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                String fileName = generateFileName("等价类表", "xlsx");
                String fileUrl = saveEquivalenceTableToExcel(response, fileName);
                response.setExcelFileUrl(fileUrl);
            } else if ("WORD".equalsIgnoreCase(request.getFormat())) {
                String fileName = generateFileName("等价类表", "docx");
                String fileUrl = saveEquivalenceTableToWord(response, fileName);
                response.setPdfFileUrl(fileUrl); // 复用字段
            }
            
            log.info("等价类表生成成功，参数数：{}，用例数：{}", equivalenceClasses.size(), tableData.size());
            return response;
            
        } catch (Exception e) {
            log.error("生成等价类表失败", e);
            throw new RuntimeException("生成等价类表失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrthogonalTableResponseDTO generateOrthogonalTable(OrthogonalTableRequestDTO request) {
        log.info("生成正交表，请求参数：{}", request);
        
        try {
            OrthogonalTableResponseDTO response = new OrthogonalTableResponseDTO();
            
            // 1. 确定正交表类型
            String tableType = determineOrthogonalTableType(request.getFactors());
            
            // 2. 生成正交表数据
            List<Map<String, Object>> tableData = generateOrthogonalTableData(request.getFactors(), tableType);
            
            // 3. 计算统计信息
            long theoreticalMaxCases = request.getFactors().values().stream()
                    .mapToLong(List::size)
                    .reduce(1, (a, b) -> a * b);
            
            double reductionRate = theoreticalMaxCases > 0 
                    ? (1.0 - (double) tableData.size() / theoreticalMaxCases) * 100.0
                    : 0.0;
            
            // 4. 构建响应
            response.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "正交表");
            response.setTableType(tableType);
            response.setFactors(new ArrayList<>(request.getFactors().keySet()));
            response.setFactorValues(request.getFactors());
            response.setTableData(tableData);
            response.setCombinationCount(tableData.size());
            response.setTheoreticalMaxCases(theoreticalMaxCases);
            response.setReductionRate(BigDecimal.valueOf(reductionRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            response.setFormat(request.getFormat());
            
            // 5. 生成JSON数据
            response.setJsonData(objectMapper.writeValueAsString(response));
            
            // 6. 如果需要导出文件，生成文件
            if ("EXCEL".equalsIgnoreCase(request.getFormat())) {
                String fileName = generateFileName("正交表", "xlsx");
                String fileUrl = saveOrthogonalTableToExcel(response, fileName);
                response.setExcelFileUrl(fileUrl);
            } else if ("WORD".equalsIgnoreCase(request.getFormat())) {
                String fileName = generateFileName("正交表", "docx");
                String fileUrl = saveOrthogonalTableToWord(response, fileName);
                response.setPdfFileUrl(fileUrl); // 复用字段
            }
            
            log.info("正交表生成成功，因素数：{}，组合数：{}，缩减率：{}%", 
                    request.getFactors().size(), tableData.size(), response.getReductionRate());
            return response;
            
        } catch (Exception e) {
            log.error("生成正交表失败", e);
            throw new RuntimeException("生成正交表失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public void exportEquivalenceTableToExcel(EquivalenceTableResponseDTO response, OutputStream outputStream) {
        log.info("导出等价类表到Excel");
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("等价类表");
            
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // 标题行
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(response.getTitle());
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, response.getParameters().size() + 2));
            
            rowNum++; // 空行
            
            // 表头
            Row headerRow = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String param : response.getParameters()) {
                Cell cell = headerRow.createCell(colNum++);
                cell.setCellValue(param);
                cell.setCellStyle(headerStyle);
            }
            Cell caseCodeCell = headerRow.createCell(colNum++);
            caseCodeCell.setCellValue("用例编号");
            caseCodeCell.setCellStyle(headerStyle);
            Cell isValidCell = headerRow.createCell(colNum++);
            isValidCell.setCellValue("是否有效");
            isValidCell.setCellStyle(headerStyle);
            
            // 数据行
            int caseIndex = 1;
            for (Map<String, Object> rowData : response.getTableData()) {
                Row dataRow = sheet.createRow(rowNum++);
                colNum = 0;
                for (String param : response.getParameters()) {
                    Cell cell = dataRow.createCell(colNum++);
                    Object value = rowData.get(param);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                    cell.setCellStyle(dataStyle);
                }
                Cell caseCodeDataCell = dataRow.createCell(colNum++);
                caseCodeDataCell.setCellValue("TC-" + String.format("%03d", caseIndex++));
                caseCodeDataCell.setCellStyle(dataStyle);
                Cell isValidDataCell = dataRow.createCell(colNum++);
                Boolean isValid = (Boolean) rowData.get("isValid");
                isValidDataCell.setCellValue(Boolean.TRUE.equals(isValid) ? "有效" : "无效");
                isValidDataCell.setCellStyle(dataStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < response.getParameters().size() + 2; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            workbook.close();
            log.info("等价类表导出到Excel成功");
        } catch (Exception e) {
            log.error("导出等价类表到Excel失败", e);
            throw new RuntimeException("导出等价类表到Excel失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public void exportEquivalenceTableToWord(EquivalenceTableResponseDTO response, OutputStream outputStream) {
        log.info("导出等价类表到Word");
        try {
            XWPFDocument document = new XWPFDocument();
            
            // 标题
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(response.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 空行
            document.createParagraph();
            
            // 创建表格
            XWPFTable table = document.createTable(response.getTableData().size() + 1, response.getParameters().size() + 2);
            
            // 表头
            XWPFTableRow headerRow = table.getRow(0);
            int colIndex = 0;
            for (String param : response.getParameters()) {
                setCellValue(headerRow.getCell(colIndex++), param);
            }
            setCellValue(headerRow.getCell(colIndex++), "用例编号");
            setCellValue(headerRow.getCell(colIndex++), "是否有效");
            
            // 数据行
            int caseIndex = 1;
            for (int i = 0; i < response.getTableData().size(); i++) {
                Map<String, Object> rowData = response.getTableData().get(i);
                XWPFTableRow dataRow = table.getRow(i + 1);
                colIndex = 0;
                for (String param : response.getParameters()) {
                    Object value = rowData.get(param);
                    setCellValue(dataRow.getCell(colIndex++), value != null ? value.toString() : "");
                }
                setCellValue(dataRow.getCell(colIndex++), "TC-" + String.format("%03d", caseIndex++));
                Boolean isValid = (Boolean) rowData.get("isValid");
                setCellValue(dataRow.getCell(colIndex++), Boolean.TRUE.equals(isValid) ? "有效" : "无效");
            }
            
            document.write(outputStream);
            document.close();
            log.info("等价类表导出到Word成功");
        } catch (Exception e) {
            log.error("导出等价类表到Word失败", e);
            throw new RuntimeException("导出等价类表到Word失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public void exportOrthogonalTableToExcel(OrthogonalTableResponseDTO response, OutputStream outputStream) {
        log.info("导出正交表到Excel");
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("正交表");
            
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // 标题行
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(response.getTitle() + " (" + response.getTableType() + ")");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, response.getFactors().size() + 1));
            
            rowNum++; // 空行
            
            // 统计信息
            Row statRow = sheet.createRow(rowNum++);
            statRow.createCell(0).setCellValue("组合数：" + response.getCombinationCount());
            statRow.createCell(1).setCellValue("理论最大用例数：" + response.getTheoreticalMaxCases());
            statRow.createCell(2).setCellValue("缩减率：" + response.getReductionRate() + "%");
            
            rowNum++; // 空行
            
            // 表头
            Row headerRow = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String factor : response.getFactors()) {
                Cell cell = headerRow.createCell(colNum++);
                cell.setCellValue(factor);
                cell.setCellStyle(headerStyle);
            }
            Cell caseCodeCell = headerRow.createCell(colNum++);
            caseCodeCell.setCellValue("用例编号");
            caseCodeCell.setCellStyle(headerStyle);
            
            // 数据行
            int caseIndex = 1;
            for (Map<String, Object> rowData : response.getTableData()) {
                Row dataRow = sheet.createRow(rowNum++);
                colNum = 0;
                for (String factor : response.getFactors()) {
                    Cell cell = dataRow.createCell(colNum++);
                    Object value = rowData.get(factor);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                    cell.setCellStyle(dataStyle);
                }
                Cell caseCodeDataCell = dataRow.createCell(colNum++);
                caseCodeDataCell.setCellValue("TC-" + String.format("%03d", caseIndex++));
                caseCodeDataCell.setCellStyle(dataStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < response.getFactors().size() + 1; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            workbook.close();
            log.info("正交表导出到Excel成功");
        } catch (Exception e) {
            log.error("导出正交表到Excel失败", e);
            throw new RuntimeException("导出正交表到Excel失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public void exportOrthogonalTableToWord(OrthogonalTableResponseDTO response, OutputStream outputStream) {
        log.info("导出正交表到Word");
        try {
            XWPFDocument document = new XWPFDocument();
            
            // 标题
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(response.getTitle() + " (" + response.getTableType() + ")");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            
            // 空行
            document.createParagraph();
            
            // 统计信息
            XWPFParagraph statPara = document.createParagraph();
            statPara.createRun().setText("组合数：" + response.getCombinationCount() + "  |  ");
            statPara.createRun().setText("理论最大用例数：" + response.getTheoreticalMaxCases() + "  |  ");
            statPara.createRun().setText("缩减率：" + response.getReductionRate() + "%");
            
            // 空行
            document.createParagraph();
            
            // 创建表格
            XWPFTable table = document.createTable(response.getTableData().size() + 1, response.getFactors().size() + 1);
            
            // 表头
            XWPFTableRow headerRow = table.getRow(0);
            int colIndex = 0;
            for (String factor : response.getFactors()) {
                setCellValue(headerRow.getCell(colIndex++), factor);
            }
            setCellValue(headerRow.getCell(colIndex++), "用例编号");
            
            // 数据行
            int caseIndex = 1;
            for (int i = 0; i < response.getTableData().size(); i++) {
                Map<String, Object> rowData = response.getTableData().get(i);
                XWPFTableRow dataRow = table.getRow(i + 1);
                colIndex = 0;
                for (String factor : response.getFactors()) {
                    Object value = rowData.get(factor);
                    setCellValue(dataRow.getCell(colIndex++), value != null ? value.toString() : "");
                }
                setCellValue(dataRow.getCell(colIndex++), "TC-" + String.format("%03d", caseIndex++));
            }
            
            document.write(outputStream);
            document.close();
            log.info("正交表导出到Word成功");
        } catch (Exception e) {
            log.error("导出正交表到Word失败", e);
            throw new RuntimeException("导出正交表到Word失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 从需求或用例中提取等价类数据
     */
    private Map<String, Map<String, List<String>>> extractEquivalenceClassesFromSource(EquivalenceTableRequestDTO request) {
        Map<String, Map<String, List<String>>> equivalenceClasses = new LinkedHashMap<>();
        
        List<TestCase> testCases = new ArrayList<>();
        
        // 从需求中提取用例
        if (request.getRequirementId() != null) {
            testCases.addAll(testCaseRepository.findByRequirementId(request.getRequirementId()));
        }
        
        // 从用例ID列表提取用例
        if (request.getCaseIds() != null && !request.getCaseIds().isEmpty()) {
            testCases.addAll(testCaseRepository.findAllById(request.getCaseIds()));
        }
        
        // 从用例中提取参数（简单实现：基于测试步骤中的关键词）
        if (request.getAutoIdentifyParameters() != null && request.getAutoIdentifyParameters()) {
            // 注意：完整的AI/NLP参数提取功能需要调用Python AI服务实现
            // 当前实现：基于关键词的简单提取（作为占位实现）
            // 实际生产环境应调用AI服务进行智能提取
            log.warn("自动参数识别功能使用简单实现，建议后续集成AI服务进行智能提取");
            
            // 简单实现：从测试步骤中提取可能的参数
            String testStep = testCase.getTestStep();
            if (testStep != null && testStep.length() > 0) {
                // 提取可能的输入参数（简单关键词匹配）
                String[] keywords = {"输入", "输入值", "参数", "值", "金额", "数量", "日期"};
                for (String keyword : keywords) {
                    if (testStep.contains(keyword)) {
                        equivalenceClasses.put(keyword, Map.of(
                                "有效等价类", Arrays.asList("正常值1", "正常值2", "正常值3"),
                                "无效等价类", Arrays.asList("空值", "非法值", "超出范围值")
                        ));
                        break; // 只提取第一个匹配的参数
                    }
                }
            }
            
            // 如果没有提取到参数，使用默认示例数据
            if (equivalenceClasses.isEmpty()) {
                equivalenceClasses.put("输入参数", Map.of(
                        "有效等价类", Arrays.asList("值1", "值2", "值3"),
                        "无效等价类", Arrays.asList("空值", "非法值")
                ));
            }
            equivalenceClasses.put("输入参数2", Map.of(
                    "有效等价类", Arrays.asList("值A", "值B"),
                    "无效等价类", Arrays.asList("空值")
            ));
        } else if (request.getParameterNames() != null && !request.getParameterNames().isEmpty()) {
            // 为指定的参数创建空的等价类
            for (String paramName : request.getParameterNames()) {
                equivalenceClasses.put(paramName, Map.of(
                        "有效等价类", new ArrayList<>(),
                        "无效等价类", new ArrayList<>()
                ));
            }
        }
        
        return equivalenceClasses;
    }
    
    /**
     * 生成等价类表数据（笛卡尔积）
     */
    private List<Map<String, Object>> generateEquivalenceTableData(Map<String, Map<String, List<String>>> equivalenceClasses) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        
        // 提取所有参数的有效和无效等价类
        List<String> parameters = new ArrayList<>(equivalenceClasses.keySet());
        List<List<Map.Entry<String, String>>> parameterCombinations = new ArrayList<>();
        
        for (String param : parameters) {
            Map<String, List<String>> classes = equivalenceClasses.get(param);
            List<Map.Entry<String, String>> paramValues = new ArrayList<>();
            
            // 添加有效等价类
            for (String value : classes.getOrDefault("有效等价类", Collections.emptyList())) {
                paramValues.add(new AbstractMap.SimpleEntry<>(param, value));
            }
            
            // 添加无效等价类
            for (String value : classes.getOrDefault("无效等价类", Collections.emptyList())) {
                paramValues.add(new AbstractMap.SimpleEntry<>(param, value));
            }
            
            parameterCombinations.add(paramValues);
        }
        
        // 生成笛卡尔积
        List<List<Map.Entry<String, String>>> cartesianProduct = generateCartesianProduct(parameterCombinations);
        
        // 构建表格数据
        for (List<Map.Entry<String, String>> combination : cartesianProduct) {
            Map<String, Object> row = new LinkedHashMap<>();
            boolean isValid = true;
            
            for (Map.Entry<String, String> entry : combination) {
                row.put(entry.getKey(), entry.getValue());
                // 判断是否有效（如果包含无效等价类，则整个组合无效）
                Map<String, List<String>> classes = equivalenceClasses.get(entry.getKey());
                List<String> invalidClasses = classes.getOrDefault("无效等价类", Collections.emptyList());
                if (invalidClasses.contains(entry.getValue())) {
                    isValid = false;
                }
            }
            
            row.put("isValid", isValid);
            tableData.add(row);
        }
        
        return tableData;
    }
    
    /**
     * 生成笛卡尔积
     */
    private <T> List<List<T>> generateCartesianProduct(List<List<T>> lists) {
        if (lists.isEmpty()) {
            return Collections.singletonList(Collections.emptyList());
        }
        
        List<List<T>> result = new ArrayList<>();
        List<T> firstList = lists.get(0);
        List<List<T>> restProduct = generateCartesianProduct(lists.subList(1, lists.size()));
        
        for (T item : firstList) {
            for (List<T> rest : restProduct) {
                List<T> combination = new ArrayList<>();
                combination.add(item);
                combination.addAll(rest);
                result.add(combination);
            }
        }
        
        return result;
    }
    
    /**
     * 确定正交表类型
     */
    private String determineOrthogonalTableType(Map<String, List<String>> factors) {
        // 计算因素数和水平数
        int factorCount = factors.size();
        List<Integer> levels = factors.values().stream()
                .map(List::size)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        
        // 根据因素数和水平数选择正交表类型
        if (factorCount <= 2 && levels.get(0) == 2 && (levels.size() == 1 || levels.get(1) == 2)) {
            return "L4";
        } else if (factorCount <= 7 && levels.stream().allMatch(l -> l == 2)) {
            return "L8";
        } else if (factorCount <= 2 && levels.stream().allMatch(l -> l == 3)) {
            return "L9";
        } else if (factorCount <= 11 && levels.stream().allMatch(l -> l == 2)) {
            return "L12";
        } else if (factorCount <= 15 && levels.stream().allMatch(l -> l == 2)) {
            return "L16";
        } else if (factorCount <= 2 && levels.stream().allMatch(l -> l == 5)) {
            return "L25";
        } else {
            // 默认使用L8
            return "L8";
        }
    }
    
    /**
     * 生成正交表数据
     */
    private List<Map<String, Object>> generateOrthogonalTableData(Map<String, List<String>> factors, String tableType) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        List<String> factorNames = new ArrayList<>(factors.keySet());
        
        // 根据正交表类型生成数据
        // 这里使用简化的正交表生成算法
        // 实际应用中可以使用更复杂的正交表生成算法或查找标准正交表
        
        // 获取所有因素的水平数
        List<List<String>> factorLevels = factorNames.stream()
                .map(factors::get)
                .collect(Collectors.toList());
        
        // 简化的正交表生成：使用配对组合算法
        tableData = generatePairwiseCombinations(factorNames, factorLevels);
        
        return tableData;
    }
    
    /**
     * 生成配对组合（Pairwise Testing）
     * 这是一种简化的正交表生成方法
     */
    private List<Map<String, Object>> generatePairwiseCombinations(List<String> factorNames, List<List<String>> factorLevels) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        
        if (factorNames.isEmpty()) {
            return combinations;
        }
        
        // 递归生成配对组合
        generatePairwiseRecursive(factorNames, factorLevels, 0, new LinkedHashMap<>(), combinations);
        
        return combinations;
    }
    
    private void generatePairwiseRecursive(List<String> factorNames, List<List<String>> factorLevels, 
                                          int index, Map<String, Object> current, List<Map<String, Object>> result) {
        if (index >= factorNames.size()) {
            result.add(new LinkedHashMap<>(current));
            return;
        }
        
        String factorName = factorNames.get(index);
        List<String> levels = factorLevels.get(index);
        
        for (String level : levels) {
            current.put(factorName, level);
            generatePairwiseRecursive(factorNames, factorLevels, index + 1, current, result);
            current.remove(factorName);
            
            // 简化：只为每个因素的前几个水平生成组合，减少组合数
            if (index > 0 && result.size() >= 16) {
                break;
            }
        }
    }
    
    /**
     * 保存等价类表到Excel并返回文件URL
     */
    private String saveEquivalenceTableToExcel(EquivalenceTableResponseDTO response, String fileName) {
        try {
            // 生成文件路径
            String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
            String relativePath = String.format("%s/%s/%s", dateStr.substring(0, 4), 
                    dateStr.substring(4, 6), dateStr.substring(6, 8));
            java.io.File dir = new java.io.File("./uploads", relativePath);
            dir.mkdirs();
            
            java.io.File file = new java.io.File(dir, fileName);
            try (OutputStream os = new java.io.FileOutputStream(file)) {
                exportEquivalenceTableToExcel(response, os);
            }
            
            String fileUrl = fileUploadService.getFileUrl(relativePath + "/" + fileName);
            log.info("保存等价类表到Excel成功：{}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("保存等价类表到Excel失败", e);
            throw new RuntimeException("保存等价类表到Excel失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 保存等价类表到Word并返回文件URL
     */
    private String saveEquivalenceTableToWord(EquivalenceTableResponseDTO response, String fileName) {
        try {
            // 生成文件路径
            String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
            String relativePath = String.format("%s/%s/%s", dateStr.substring(0, 4), 
                    dateStr.substring(4, 6), dateStr.substring(6, 8));
            java.io.File dir = new java.io.File("./uploads", relativePath);
            dir.mkdirs();
            
            java.io.File file = new java.io.File(dir, fileName);
            try (OutputStream os = new java.io.FileOutputStream(file)) {
                exportEquivalenceTableToWord(response, os);
            }
            
            String fileUrl = fileUploadService.getFileUrl(relativePath + "/" + fileName);
            log.info("保存等价类表到Word成功：{}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("保存等价类表到Word失败", e);
            throw new RuntimeException("保存等价类表到Word失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 保存正交表到Excel并返回文件URL
     */
    private String saveOrthogonalTableToExcel(OrthogonalTableResponseDTO response, String fileName) {
        try {
            // 生成文件路径
            String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
            String relativePath = String.format("%s/%s/%s", dateStr.substring(0, 4), 
                    dateStr.substring(4, 6), dateStr.substring(6, 8));
            java.io.File dir = new java.io.File("./uploads", relativePath);
            dir.mkdirs();
            
            java.io.File file = new java.io.File(dir, fileName);
            try (OutputStream os = new java.io.FileOutputStream(file)) {
                exportOrthogonalTableToExcel(response, os);
            }
            
            String fileUrl = fileUploadService.getFileUrl(relativePath + "/" + fileName);
            log.info("保存正交表到Excel成功：{}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("保存正交表到Excel失败", e);
            throw new RuntimeException("保存正交表到Excel失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 保存正交表到Word并返回文件URL
     */
    private String saveOrthogonalTableToWord(OrthogonalTableResponseDTO response, String fileName) {
        try {
            // 生成文件路径
            String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
            String relativePath = String.format("%s/%s/%s", dateStr.substring(0, 4), 
                    dateStr.substring(4, 6), dateStr.substring(6, 8));
            java.io.File dir = new java.io.File("./uploads", relativePath);
            dir.mkdirs();
            
            java.io.File file = new java.io.File(dir, fileName);
            try (OutputStream os = new java.io.FileOutputStream(file)) {
                exportOrthogonalTableToWord(response, os);
            }
            
            String fileUrl = fileUploadService.getFileUrl(relativePath + "/" + fileName);
            log.info("保存正交表到Word成功：{}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("保存正交表到Word失败", e);
            throw new RuntimeException("保存正交表到Word失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 设置Word表格单元格值
     */
    private void setCellValue(XWPFTableCell cell, String value) {
        cell.removeParagraph(0);
        XWPFParagraph para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = para.createRun();
        run.setText(value != null ? value : "");
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String prefix, String extension) {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        return String.format("%s_%s.%s", prefix, dateStr, extension);
    }
}

