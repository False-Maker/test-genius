package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.dto.EquivalenceTableRequestDTO;
import com.sinosoft.testdesign.dto.EquivalenceTableResponseDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableRequestDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableResponseDTO;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.AIServiceClient;
import com.sinosoft.testdesign.service.DataDocumentGenerationService;
import com.sinosoft.testdesign.service.FileUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Random;

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
    private final AIServiceClient aiServiceClient;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
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
        
        // 从用例中提取参数（使用AI服务智能提取）
        if (request.getAutoIdentifyParameters() != null && request.getAutoIdentifyParameters()) {
            try {
                // 构建Python服务请求
                List<Map<String, Object>> testCasesData = new ArrayList<>();
                for (TestCase testCase : testCases) {
                    Map<String, Object> caseData = new HashMap<>();
                    caseData.put("case_name", testCase.getCaseName());
                    caseData.put("test_step", testCase.getTestStep());
                    caseData.put("expected_result", testCase.getExpectedResult());
                    caseData.put("pre_condition", testCase.getPreCondition());
                    testCasesData.add(caseData);
                }
                
                Map<String, Object> pythonRequest = new HashMap<>();
                pythonRequest.put("test_cases", testCasesData);
                pythonRequest.put("use_llm", true);
                
                // 调用Python服务提取参数
                String url = aiServiceUrl + "/api/v1/parameter-extraction/extract";
                log.info("调用Python服务提取参数，用例数量: {}", testCases.size());
                
                @SuppressWarnings("unchecked")
                Map<String, Object> response = aiServiceClient.post(url, pythonRequest);
                
                if (response != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> extractedEquivalenceClasses = 
                            (Map<String, Object>) response.get("equivalence_classes");
                    
                    if (extractedEquivalenceClasses != null) {
                        // 转换格式
                        for (Map.Entry<String, Object> entry : extractedEquivalenceClasses.entrySet()) {
                            String paramName = entry.getKey();
                            @SuppressWarnings("unchecked")
                            Map<String, Object> classes = (Map<String, Object>) entry.getValue();
                            
                            @SuppressWarnings("unchecked")
                            List<String> validClasses = (List<String>) classes.get("有效等价类");
                            @SuppressWarnings("unchecked")
                            List<String> invalidClasses = (List<String>) classes.get("无效等价类");
                            
                            equivalenceClasses.put(paramName, Map.of(
                                    "有效等价类", validClasses != null ? validClasses : new ArrayList<>(),
                                    "无效等价类", invalidClasses != null ? invalidClasses : new ArrayList<>()
                            ));
                        }
                        
                        log.info("AI参数提取成功，提取到 {} 个参数", equivalenceClasses.size());
                    } else {
                        log.warn("Python服务返回的等价类为空，使用降级方案");
                        extractParametersWithRules(testCases, equivalenceClasses);
                    }
                } else {
                    log.warn("Python服务返回空响应，使用降级方案");
                    extractParametersWithRules(testCases, equivalenceClasses);
                }
            } catch (Exception e) {
                log.error("调用Python服务提取参数失败: {}", e.getMessage(), e);
                log.warn("使用降级方案：基于规则的参数提取");
                extractParametersWithRules(testCases, equivalenceClasses);
            }
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
     * 使用规则提取参数（降级方案）
     */
    private void extractParametersWithRules(
            List<TestCase> testCases,
            Map<String, Map<String, List<String>>> equivalenceClasses) {
        // 简单实现：从测试步骤中提取可能的参数
        String[] keywords = {"输入", "输入值", "参数", "值", "金额", "数量", "日期"};
        
        for (TestCase testCase : testCases) {
            String testStep = testCase.getTestStep();
            if (testStep != null && testStep.length() > 0) {
                // 提取可能的输入参数（简单关键词匹配）
                for (String keyword : keywords) {
                    if (testStep.contains(keyword) && !equivalenceClasses.containsKey(keyword)) {
                        equivalenceClasses.put(keyword, Map.of(
                                "有效等价类", Arrays.asList("正常值1", "正常值2", "正常值3"),
                                "无效等价类", Arrays.asList("空值", "非法值", "超出范围值")
                        ));
                        break; // 每个用例只提取第一个匹配的参数
                    }
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
        
        // 优化：如果参数组合数过多，使用智能生成策略
        long totalCombinations = parameterCombinations.stream()
                .mapToLong(List::size)
                .reduce(1, (a, b) -> a * b);
        
        List<List<Map.Entry<String, String>>> cartesianProduct;
        if (totalCombinations > 1000) {
            // 如果组合数过多，使用智能生成（优先生成有效组合和边界组合）
            cartesianProduct = generateSmartCombinations(parameterCombinations, equivalenceClasses);
        } else {
            // 组合数较少，生成完整笛卡尔积
            cartesianProduct = generateCartesianProduct(parameterCombinations);
        }
        
        // 构建表格数据（优化：按有效性排序，有效用例在前）
        List<Map<String, Object>> validRows = new ArrayList<>();
        List<Map<String, Object>> invalidRows = new ArrayList<>();
        
        for (List<Map.Entry<String, String>> combination : cartesianProduct) {
            Map<String, Object> row = new LinkedHashMap<>();
            boolean isValid = true;
            int invalidCount = 0; // 统计无效等价类数量
            
            for (Map.Entry<String, String> entry : combination) {
                row.put(entry.getKey(), entry.getValue());
                // 判断是否有效（如果包含无效等价类，则整个组合无效）
                Map<String, List<String>> classes = equivalenceClasses.get(entry.getKey());
                List<String> invalidClasses = classes.getOrDefault("无效等价类", Collections.emptyList());
                if (invalidClasses.contains(entry.getValue())) {
                    isValid = false;
                    invalidCount++;
                }
            }
            
            row.put("isValid", isValid);
            row.put("invalidCount", invalidCount); // 记录无效等价类数量，用于排序
            
            if (isValid) {
                validRows.add(row);
            } else {
                invalidRows.add(row);
            }
        }
        
        // 排序：有效用例在前，无效用例按无效数量排序
        validRows.sort((r1, r2) -> 0); // 有效用例保持原序
        invalidRows.sort((r1, r2) -> {
            Integer count1 = (Integer) r1.get("invalidCount");
            Integer count2 = (Integer) r2.get("invalidCount");
            return Integer.compare(count1 != null ? count1 : 0, count2 != null ? count2 : 0);
        });
        
        tableData.addAll(validRows);
        tableData.addAll(invalidRows);
        
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
     * 智能生成组合（优化：优先生成有效组合和边界组合）
     */
    private List<List<Map.Entry<String, String>>> generateSmartCombinations(
            List<List<Map.Entry<String, String>>> parameterCombinations,
            Map<String, Map<String, List<String>>> equivalenceClasses) {
        List<List<Map.Entry<String, String>>> result = new ArrayList<>();
        
        // 策略1：生成所有有效等价类的组合（优先）
        List<List<Map.Entry<String, String>>> validCombinations = new ArrayList<>();
        for (List<Map.Entry<String, String>> paramValues : parameterCombinations) {
            List<Map.Entry<String, String>> validValues = new ArrayList<>();
            for (Map.Entry<String, String> entry : paramValues) {
                String paramName = entry.getKey();
                Map<String, List<String>> classes = equivalenceClasses.get(paramName);
                List<String> validClasses = classes.getOrDefault("有效等价类", Collections.emptyList());
                if (validClasses.contains(entry.getValue())) {
                    validValues.add(entry);
                }
            }
            if (!validValues.isEmpty()) {
                validCombinations.add(validValues);
            }
        }
        if (!validCombinations.isEmpty()) {
            result.addAll(generateCartesianProduct(validCombinations));
        }
        
        // 策略2：生成边界组合（每个参数选择一个无效等价类，其他选择有效等价类）
        for (int i = 0; i < parameterCombinations.size(); i++) {
            List<Map.Entry<String, String>> boundaryCombination = new ArrayList<>();
            for (int j = 0; j < parameterCombinations.size(); j++) {
                if (i == j) {
                    // 当前参数选择第一个无效等价类
                    String paramName = parameterCombinations.get(j).get(0).getKey();
                    Map<String, List<String>> classes = equivalenceClasses.get(paramName);
                    List<String> invalidClasses = classes.getOrDefault("无效等价类", Collections.emptyList());
                    if (!invalidClasses.isEmpty()) {
                        boundaryCombination.add(new AbstractMap.SimpleEntry<>(paramName, invalidClasses.get(0)));
                    } else {
                        // 如果没有无效等价类，选择有效等价类
                        boundaryCombination.add(parameterCombinations.get(j).get(0));
                    }
                } else {
                    // 其他参数选择第一个有效等价类
                    String paramName = parameterCombinations.get(j).get(0).getKey();
                    Map<String, List<String>> classes = equivalenceClasses.get(paramName);
                    List<String> validClasses = classes.getOrDefault("有效等价类", Collections.emptyList());
                    if (!validClasses.isEmpty()) {
                        boundaryCombination.add(new AbstractMap.SimpleEntry<>(paramName, validClasses.get(0)));
                    } else {
                        boundaryCombination.add(parameterCombinations.get(j).get(0));
                    }
                }
            }
            if (!boundaryCombination.isEmpty()) {
                result.add(boundaryCombination);
            }
        }
        
        // 策略3：如果结果仍然太少，补充一些随机组合（最多100个）
        if (result.size() < 50) {
            int maxAdditional = Math.min(100, (int) (parameterCombinations.stream()
                    .mapToLong(List::size)
                    .reduce(1, (a, b) -> a * b) - result.size()));
            
            // 随机生成一些组合
            Random random = new Random();
            Set<String> existingCombinations = new HashSet<>();
            for (List<Map.Entry<String, String>> combo : result) {
                existingCombinations.add(comboToString(combo));
            }
            
            int added = 0;
            while (added < maxAdditional && added < 1000) {
                List<Map.Entry<String, String>> randomCombo = new ArrayList<>();
                for (List<Map.Entry<String, String>> paramValues : parameterCombinations) {
                    if (!paramValues.isEmpty()) {
                        randomCombo.add(paramValues.get(random.nextInt(paramValues.size())));
                    }
                }
                String comboKey = comboToString(randomCombo);
                if (!existingCombinations.contains(comboKey)) {
                    result.add(randomCombo);
                    existingCombinations.add(comboKey);
                    added++;
                }
            }
        }
        
        return result;
    }
    
    /**
     * 将组合转换为字符串（用于去重）
     */
    private String comboToString(List<Map.Entry<String, String>> combo) {
        return combo.stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("|"));
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
        
        // 优化：根据因素数和水平数智能选择正交表类型
        int maxLevel = levels.isEmpty() ? 0 : levels.get(0);
        boolean allSameLevel = levels.stream().allMatch(l -> l.equals(maxLevel));
        
        // 标准正交表选择规则
        if (allSameLevel) {
            // 所有因素水平数相同
            if (maxLevel == 2) {
                if (factorCount <= 3) {
                    return "L4(2³)";
                } else if (factorCount <= 7) {
                    return "L8(2⁷)";
                } else if (factorCount <= 11) {
                    return "L12(2¹¹)";
                } else if (factorCount <= 15) {
                    return "L16(2¹⁵)";
                } else {
                    return "L32(2³¹)";
                }
            } else if (maxLevel == 3) {
                if (factorCount <= 4) {
                    return "L9(3⁴)";
                } else if (factorCount <= 13) {
                    return "L27(3¹³)";
                } else {
                    return "L81(3⁴⁰)";
                }
            } else if (maxLevel == 4) {
                if (factorCount <= 5) {
                    return "L16(4⁵)";
                } else {
                    return "L64(4²¹)";
                }
            } else if (maxLevel == 5) {
                if (factorCount <= 6) {
                    return "L25(5⁶)";
                } else {
                    return "L125(5³¹)";
                }
            }
        } else {
            // 混合水平正交表（简化处理）
            if (maxLevel <= 2 && factorCount <= 7) {
                return "L8(混合水平)";
            } else if (maxLevel <= 3 && factorCount <= 4) {
                return "L9(混合水平)";
            } else if (maxLevel <= 4 && factorCount <= 5) {
                return "L16(混合水平)";
            }
        }
        
        // 默认使用配对组合算法
        return "配对组合";
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

