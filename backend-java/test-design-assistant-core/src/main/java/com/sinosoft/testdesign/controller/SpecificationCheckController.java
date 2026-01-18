package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestSpecification;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import com.sinosoft.testdesign.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规约检查控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/specification-check")
@RequiredArgsConstructor
@Tag(name = "规约检查", description = "规约检查和规约应用相关API")
public class SpecificationCheckController {
    
    private final SpecificationCheckService specificationCheckService;
    private final TestCaseService testCaseService;
    
    @Operation(summary = "自动匹配适用的规约", description = "根据用例的模块、测试分层、测试方法自动匹配适用的规约")
    @GetMapping("/match/{caseId}")
    public Result<List<SpecificationSummaryDTO>> matchSpecifications(@PathVariable Long caseId) {
        log.info("自动匹配适用的规约，用例ID: {}", caseId);
        
        TestCase testCase = testCaseService.getTestCaseById(caseId);
        List<TestSpecification> matchedSpecs = specificationCheckService.matchSpecifications(testCase);
        
        List<SpecificationSummaryDTO> dtoList = matchedSpecs.stream()
                .map(this::toSpecificationSummaryDTO)
                .collect(Collectors.toList());
        
        return Result.success(dtoList);
    }
    
    @Operation(summary = "检查用例是否符合规约", description = "检查用例是否符合规约要求，返回符合性检查结果")
    @PostMapping("/check")
    public Result<SpecificationCheckResponseDTO> checkCompliance(
            @Valid @RequestBody SpecificationCheckRequestDTO request) {
        log.info("检查用例规约符合性，用例ID: {}", request.getCaseId());
        
        TestCase testCase = testCaseService.getTestCaseById(request.getCaseId());
        
        // 如果指定了规约ID列表，则查询这些规约；否则自动匹配
        List<TestSpecification> specifications = null;
        if (request.getSpecificationIds() != null && !request.getSpecificationIds().isEmpty()) {
            // TODO: 需要添加根据ID列表查询规约的方法
            specifications = new ArrayList<>();
        }
        
        SpecificationCheckService.SpecificationComplianceResult result = 
                specificationCheckService.checkCompliance(testCase, specifications);
        
        // 自动匹配规约列表
        List<TestSpecification> matchedSpecs = specificationCheckService.matchSpecifications(testCase);
        
        // 转换为DTO
        SpecificationCheckResponseDTO response = toSpecificationCheckResponseDTO(result, matchedSpecs);
        
        return Result.success(response);
    }
    
    @Operation(summary = "注入规约内容到用例", description = "在用例生成时将规约内容注入到用例中")
    @PostMapping("/inject")
    public Result<SpecificationInjectionResponseDTO> injectSpecification(
            @Valid @RequestBody SpecificationCheckRequestDTO request) {
        log.info("注入规约内容到用例，用例ID: {}", request.getCaseId());
        
        TestCase testCase = testCaseService.getTestCaseById(request.getCaseId());
        
        // 如果指定了规约ID列表，则查询这些规约；否则自动匹配
        List<TestSpecification> specifications = null;
        if (request.getSpecificationIds() != null && !request.getSpecificationIds().isEmpty()) {
            // TODO: 需要添加根据ID列表查询规约的方法
            specifications = new ArrayList<>();
        }
        
        SpecificationCheckService.SpecificationInjectionResult result = 
                specificationCheckService.injectSpecification(testCase, specifications);
        
        // 转换为DTO
        SpecificationInjectionResponseDTO response = toSpecificationInjectionResponseDTO(result);
        
        return Result.success(response);
    }
    
    @Operation(summary = "生成规约符合性报告", description = "生成详细的规约符合性检查报告")
    @PostMapping("/report")
    public Result<SpecificationComplianceReportDTO> generateComplianceReport(
            @Valid @RequestBody SpecificationCheckRequestDTO request) {
        log.info("生成规约符合性报告，用例ID: {}", request.getCaseId());
        
        TestCase testCase = testCaseService.getTestCaseById(request.getCaseId());
        
        // 如果指定了规约ID列表，则查询这些规约；否则自动匹配
        List<TestSpecification> specifications = null;
        if (request.getSpecificationIds() != null && !request.getSpecificationIds().isEmpty()) {
            // TODO: 需要添加根据ID列表查询规约的方法
            specifications = new ArrayList<>();
        }
        
        // 检查符合性
        SpecificationCheckService.SpecificationComplianceResult checkResult = 
                specificationCheckService.checkCompliance(testCase, specifications);
        
        // 自动匹配规约列表
        List<TestSpecification> matchedSpecs = specificationCheckService.matchSpecifications(testCase);
        
        // 生成报告
        SpecificationComplianceReportDTO report = generateReport(testCase, checkResult, matchedSpecs);
        
        return Result.success(report);
    }
    
    /**
     * 转换为规约摘要DTO
     */
    private SpecificationSummaryDTO toSpecificationSummaryDTO(TestSpecification spec) {
        SpecificationSummaryDTO dto = new SpecificationSummaryDTO();
        dto.setId(spec.getId());
        dto.setSpecCode(spec.getSpecCode());
        dto.setSpecName(spec.getSpecName());
        dto.setSpecType(spec.getSpecType());
        dto.setCurrentVersion(spec.getCurrentVersion());
        return dto;
    }
    
    /**
     * 转换为规约检查响应DTO
     */
    private SpecificationCheckResponseDTO toSpecificationCheckResponseDTO(
            SpecificationCheckService.SpecificationComplianceResult result,
            List<TestSpecification> matchedSpecs) {
        SpecificationCheckResponseDTO dto = new SpecificationCheckResponseDTO();
        dto.setIsCompliant(result.isCompliant());
        dto.setComplianceScore(result.getComplianceScore());
        dto.setTotalChecks(result.getTotalChecks());
        dto.setPassedChecks(result.getPassedChecks());
        dto.setFailedChecks(result.getFailedChecks());
        
        // 转换问题列表
        List<ComplianceIssueDTO> issueDTOs = result.getIssues().stream()
                .map(this::toComplianceIssueDTO)
                .collect(Collectors.toList());
        dto.setIssues(issueDTOs);
        
        // 转换匹配的规约列表
        List<SpecificationSummaryDTO> specDTOs = matchedSpecs.stream()
                .map(this::toSpecificationSummaryDTO)
                .collect(Collectors.toList());
        dto.setMatchedSpecifications(specDTOs);
        
        return dto;
    }
    
    /**
     * 转换为符合性检查问题DTO
     */
    private ComplianceIssueDTO toComplianceIssueDTO(
            SpecificationCheckService.ComplianceIssue issue) {
        ComplianceIssueDTO dto = new ComplianceIssueDTO();
        dto.setSpecCode(issue.getSpecCode());
        dto.setSpecName(issue.getSpecName());
        dto.setIssueType(issue.getIssueType());
        dto.setIssueDescription(issue.getIssueDescription());
        dto.setSeverity(issue.getSeverity());
        dto.setSuggestion(issue.getSuggestion());
        return dto;
    }
    
    /**
     * 转换为规约注入响应DTO
     */
    private SpecificationInjectionResponseDTO toSpecificationInjectionResponseDTO(
            SpecificationCheckService.SpecificationInjectionResult result) {
        SpecificationInjectionResponseDTO dto = new SpecificationInjectionResponseDTO();
        
        // 转换增强后的用例
        EnhancedTestCaseDTO enhancedDTO = new EnhancedTestCaseDTO();
        TestCase enhanced = result.getEnhancedTestCase();
        enhancedDTO.setCaseCode(enhanced.getCaseCode());
        enhancedDTO.setCaseName(enhanced.getCaseName());
        enhancedDTO.setPreCondition(enhanced.getPreCondition());
        enhancedDTO.setTestStep(enhanced.getTestStep());
        enhancedDTO.setExpectedResult(enhanced.getExpectedResult());
        dto.setEnhancedTestCase(enhancedDTO);
        
        dto.setInjectedContents(result.getInjectedContents());
        dto.setAppliedSpecs(result.getAppliedSpecs());
        
        return dto;
    }
    
    /**
     * 生成规约符合性报告
     */
    private SpecificationComplianceReportDTO generateReport(
            TestCase testCase,
            SpecificationCheckService.SpecificationComplianceResult checkResult,
            List<TestSpecification> matchedSpecs) {
        SpecificationComplianceReportDTO report = new SpecificationComplianceReportDTO();
        report.setReportCode("REPORT-" + System.currentTimeMillis());
        report.setCaseId(testCase.getId());
        report.setCaseCode(testCase.getCaseCode());
        report.setCaseName(testCase.getCaseName());
        report.setCheckTime(LocalDateTime.now());
        report.setIsCompliant(checkResult.isCompliant());
        report.setComplianceScore(checkResult.getComplianceScore());
        report.setTotalChecks(checkResult.getTotalChecks());
        report.setPassedChecks(checkResult.getPassedChecks());
        report.setFailedChecks(checkResult.getFailedChecks());
        
        // 转换问题列表
        List<ComplianceIssueDTO> issueDTOs = checkResult.getIssues().stream()
                .map(this::toComplianceIssueDTO)
                .collect(Collectors.toList());
        report.setIssues(issueDTOs);
        
        // 转换匹配的规约列表
        List<SpecificationSummaryDTO> specDTOs = matchedSpecs.stream()
                .map(this::toSpecificationSummaryDTO)
                .collect(Collectors.toList());
        report.setMatchedSpecifications(specDTOs);
        
        // 生成摘要
        String summary = String.format("用例 %s 规约符合性检查完成：符合度评分 %.2f，通过 %d/%d 项检查。",
                testCase.getCaseCode(), checkResult.getComplianceScore(),
                checkResult.getPassedChecks(), checkResult.getTotalChecks());
        report.setSummary(summary);
        
        return report;
    }
}

