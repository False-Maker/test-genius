package com.sinosoft.testdesign.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.TestCaseExcelDTO;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestDesignMethod;
import com.sinosoft.testdesign.entity.TestLayer;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.repository.TestMethodRepository;
import com.sinosoft.testdesign.repository.TestLayerRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.service.TestCaseImportExportService;
import com.sinosoft.testdesign.service.TestCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试用例导入导出服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseImportExportServiceImpl implements TestCaseImportExportService {
    
    private final RequirementRepository requirementRepository;
    private final TestLayerRepository testLayerRepository;
    private final TestMethodRepository testMethodRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseService testCaseService;
    
    private static final String CASE_CODE_PREFIX = "CASE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    public void exportToExcel(List<TestCase> testCases, OutputStream outputStream) throws IOException {
        log.info("开始导出用例到Excel，用例数量: {}", testCases.size());
        
        // 转换为Excel DTO
        List<TestCaseExcelDTO> excelData = testCases.stream()
                .map(this::convertToExcelDTO)
                .collect(Collectors.toList());
        
        // 写入Excel
        EasyExcel.write(outputStream, TestCaseExcelDTO.class)
                .sheet("测试用例")
                .doWrite(excelData);
        
        log.info("用例导出完成，导出数量: {}", excelData.size());
    }
    
    @Override
    public void exportTemplate(OutputStream outputStream) throws IOException {
        log.info("开始导出用例模板");
        
        // 创建示例数据（仅用于展示模板格式）
        List<TestCaseExcelDTO> templateData = new ArrayList<>();
        TestCaseExcelDTO example = new TestCaseExcelDTO();
        example.setCaseCode("CASE-20240101-001");
        example.setCaseName("示例用例名称");
        example.setRequirementCode("REQ-20240101-001");
        example.setLayerName("功能测试");
        example.setMethodName("等价类划分");
        example.setCaseType("正常");
        example.setCasePriority("高");
        example.setPreCondition("系统已登录");
        example.setTestStep("1. 输入有效数据\n2. 点击提交按钮");
        example.setExpectedResult("提交成功，显示成功提示");
        example.setCaseStatus("草稿");
        example.setVersion(1);
        templateData.add(example);
        
        // 写入Excel模板
        EasyExcel.write(outputStream, TestCaseExcelDTO.class)
                .sheet("测试用例模板")
                .doWrite(templateData);
        
        log.info("用例模板导出完成");
    }
    
    @Override
    @Transactional
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        log.info("开始从Excel导入用例，文件名: {}", file.getOriginalFilename());
        
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传的文件不能为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("文件格式不正确，仅支持 .xlsx 和 .xls 格式");
        }
        
        // 读取Excel数据
        List<TestCaseExcelDTO> excelData = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        
        try {
            EasyExcel.read(file.getInputStream(), TestCaseExcelDTO.class, 
                    new TestCaseExcelListener(excelData, errorMessages))
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        
        // 转换为TestCase并保存
        List<TestCase> importedCases = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (int i = 0; i < excelData.size(); i++) {
            TestCaseExcelDTO excelDTO = excelData.get(i);
            int rowNum = i + 2; // Excel行号（从第2行开始，第1行是表头）
            
            try {
                TestCase testCase = convertToTestCase(excelDTO, rowNum);
                // 保存用例
                TestCase savedCase = testCaseService.createTestCase(testCase);
                importedCases.add(savedCase);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                String errorMsg = String.format("第%d行数据导入失败: %s", rowNum, e.getMessage());
                errorMessages.add(errorMsg);
                log.error(errorMsg, e);
            }
        }
        
        log.info("用例导入完成，成功: {}, 失败: {}", successCount, failureCount);
        
        return new ImportResult(successCount, failureCount, errorMessages, importedCases);
    }
    
    /**
     * 将TestCase转换为Excel DTO
     */
    private TestCaseExcelDTO convertToExcelDTO(TestCase testCase) {
        TestCaseExcelDTO dto = new TestCaseExcelDTO();
        dto.setCaseCode(testCase.getCaseCode());
        dto.setCaseName(testCase.getCaseName());
        
        // 查询需求编码
        if (testCase.getRequirementId() != null) {
            requirementRepository.findById(testCase.getRequirementId())
                    .ifPresent(req -> dto.setRequirementCode(req.getRequirementCode()));
        }
        
        // 查询测试分层名称
        if (testCase.getLayerId() != null) {
            testLayerRepository.findById(testCase.getLayerId())
                    .ifPresent(layer -> dto.setLayerName(layer.getLayerName()));
        }
        
        // 查询测试方法名称
        if (testCase.getMethodId() != null) {
            testMethodRepository.findById(testCase.getMethodId())
                    .ifPresent(method -> dto.setMethodName(method.getMethodName()));
        }
        
        dto.setCaseType(testCase.getCaseType());
        dto.setCasePriority(testCase.getCasePriority());
        dto.setPreCondition(testCase.getPreCondition());
        dto.setTestStep(testCase.getTestStep());
        dto.setExpectedResult(testCase.getExpectedResult());
        dto.setCaseStatus(testCase.getCaseStatus());
        dto.setVersion(testCase.getVersion());
        
        return dto;
    }
    
    /**
     * 将Excel DTO转换为TestCase
     */
    private TestCase convertToTestCase(TestCaseExcelDTO dto, int rowNum) {
        TestCase testCase = new TestCase();
        
        // 用例编码（如果为空，自动生成）
        if (StringUtils.hasText(dto.getCaseCode())) {
            testCase.setCaseCode(dto.getCaseCode());
        } else {
            testCase.setCaseCode(generateCaseCode());
        }
        
        // 用例名称（必填）
        if (!StringUtils.hasText(dto.getCaseName())) {
            throw new BusinessException("用例名称不能为空");
        }
        testCase.setCaseName(dto.getCaseName());
        
        // 需求ID（通过需求编码查询）
        if (StringUtils.hasText(dto.getRequirementCode())) {
            TestRequirement requirement = requirementRepository.findByRequirementCode(dto.getRequirementCode())
                    .orElseThrow(() -> new BusinessException("需求编码不存在: " + dto.getRequirementCode()));
            testCase.setRequirementId(requirement.getId());
        }
        
        // 测试分层ID（通过分层名称查询）
        if (StringUtils.hasText(dto.getLayerName())) {
            TestLayer layer = testLayerRepository.findByLayerName(dto.getLayerName())
                    .orElseThrow(() -> new BusinessException("测试分层不存在: " + dto.getLayerName()));
            testCase.setLayerId(layer.getId());
        }
        
        // 测试方法ID（通过方法名称查询）
        if (StringUtils.hasText(dto.getMethodName())) {
            TestDesignMethod method = testMethodRepository.findByMethodName(dto.getMethodName())
                    .orElseThrow(() -> new BusinessException("测试方法不存在: " + dto.getMethodName()));
            testCase.setMethodId(method.getId());
        }
        
        // 用例类型（验证）
        if (StringUtils.hasText(dto.getCaseType())) {
            String type = dto.getCaseType();
            if (!type.equals("正常") && !type.equals("异常") && !type.equals("边界")) {
                throw new BusinessException("用例类型必须是：正常、异常或边界");
            }
            testCase.setCaseType(type);
        }
        
        // 用例优先级（验证）
        if (StringUtils.hasText(dto.getCasePriority())) {
            String priority = dto.getCasePriority();
            if (!priority.equals("高") && !priority.equals("中") && !priority.equals("低")) {
                throw new BusinessException("用例优先级必须是：高、中或低");
            }
            testCase.setCasePriority(priority);
        }
        
        testCase.setPreCondition(dto.getPreCondition());
        testCase.setTestStep(dto.getTestStep());
        testCase.setExpectedResult(dto.getExpectedResult());
        
        // 用例状态（默认草稿）
        if (StringUtils.hasText(dto.getCaseStatus())) {
            // 验证状态值
            try {
                CaseStatus.fromString(dto.getCaseStatus());
                testCase.setCaseStatus(dto.getCaseStatus());
            } catch (Exception e) {
                throw new BusinessException("用例状态值不正确: " + dto.getCaseStatus());
            }
        } else {
            testCase.setCaseStatus(CaseStatus.DRAFT.name());
        }
        
        // 版本号（默认1）
        if (dto.getVersion() != null && dto.getVersion() > 0) {
            testCase.setVersion(dto.getVersion());
        } else {
            testCase.setVersion(1);
        }
        
        return testCase;
    }
    
    /**
     * 生成用例编码
     * 格式：CASE-YYYYMMDD-序号（如 CASE-20240101-001）
     */
    /**
     * 生成用例编码
     * 格式：CASE-YYYYMMDD-序号（如 CASE-20240101-001）
     * 优化：使用数据库查询替代全量查询，提高性能
     */
    private String generateCaseCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = CASE_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的最大序号（使用数据库查询，避免全量加载）
        List<TestCase> todayCases = testCaseRepository.findByCaseCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestCase c : todayCases) {
            String code = c.getCaseCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                    log.warn("用例编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        String caseCode = prefix + String.format("%03d", newSequence);
        log.debug("生成用例编码: {}", caseCode);
        return caseCode;
    }
    
    /**
     * Excel读取监听器
     */
    private static class TestCaseExcelListener extends AnalysisEventListener<TestCaseExcelDTO> {
        private final List<TestCaseExcelDTO> dataList;
        private final List<String> errorMessages;
        
        public TestCaseExcelListener(List<TestCaseExcelDTO> dataList, List<String> errorMessages) {
            this.dataList = dataList;
            this.errorMessages = errorMessages;
        }
        
        @Override
        public void invoke(TestCaseExcelDTO data, AnalysisContext context) {
            dataList.add(data);
        }
        
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 读取完成
        }
        
        @Override
        public void onException(Exception exception, AnalysisContext context) {
            if (exception instanceof ExcelDataConvertException) {
                ExcelDataConvertException excelException = (ExcelDataConvertException) exception;
                int rowNum = excelException.getRowIndex() + 1;
                String errorMsg = String.format("第%d行数据格式错误: %s", rowNum, exception.getMessage());
                errorMessages.add(errorMsg);
            } else {
                String errorMsg = String.format("读取Excel数据时发生错误: %s", exception.getMessage());
                errorMessages.add(errorMsg);
            }
        }
    }
}

