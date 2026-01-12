package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.service.TestCaseQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用例质量评估控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "用例质量评估", description = "测试用例质量评估相关接口")
@RestController
@RequestMapping("/v1/test-case-quality")
@RequiredArgsConstructor
public class TestCaseQualityController {
    
    private final TestCaseQualityService qualityService;
    private final TestCaseRepository testCaseRepository;
    
    @Operation(summary = "评估用例质量", description = "综合评估测试用例质量，返回质量评分")
    @GetMapping("/assess/{caseId}")
    public Result<TestCaseQualityService.QualityScore> assessQuality(@PathVariable Long caseId) {
        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("用例不存在"));
        
        TestCaseQualityService.QualityScore score = qualityService.assessQuality(testCase);
        return Result.success(score);
    }
    
    @Operation(summary = "检查用例完整性", description = "检查测试用例的完整性，返回完整性评分")
    @GetMapping("/completeness/{caseId}")
    public Result<TestCaseQualityService.CompletenessScore> checkCompleteness(@PathVariable Long caseId) {
        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("用例不存在"));
        
        TestCaseQualityService.CompletenessScore score = qualityService.checkCompleteness(testCase);
        return Result.success(score);
    }
    
    @Operation(summary = "检查用例规范性", description = "检查测试用例的规范性，返回规范性评分")
    @GetMapping("/standardization/{caseId}")
    public Result<TestCaseQualityService.StandardizationScore> checkStandardization(@PathVariable Long caseId) {
        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("用例不存在"));
        
        TestCaseQualityService.StandardizationScore score = qualityService.checkStandardization(testCase);
        return Result.success(score);
    }
}

