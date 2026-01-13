package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.common.TestDataBuilder;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.service.TestCaseQualityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用例质量评估Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("用例质量评估Controller测试")
class TestCaseQualityControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestCaseQualityService qualityService;
    
    @MockBean
    private TestCaseRepository testCaseRepository;
    
    @Test
    @DisplayName("评估用例质量-成功")
    void testAssessQuality_Success() throws Exception {
        // Given
        Long caseId = 1L;
        TestCase testCase = TestDataBuilder.testCase()
            .withId(caseId)
            .withName("测试用例")
            .withSteps("1. 步骤一\n2. 步骤二")
            .withExpectedResult("预期结果")
            .build();
        
        TestCaseQualityService.QualityScore score = new TestCaseQualityService.QualityScore();
        score.setTotalScore(85.0);
        score.setCompletenessScore(90.0);
        score.setStandardizationScore(80.0);
        score.setExecutabilityScore(85.0);
        score.setQualityLevel("良好");
        
        when(testCaseRepository.findById(caseId))
            .thenReturn(Optional.of(testCase));
        when(qualityService.assessQuality(any(TestCase.class)))
            .thenReturn(score);
        
        // When & Then
        mockMvc.perform(get("/v1/test-case-quality/assess/{caseId}", caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalScore").value(85.0))
                .andExpect(jsonPath("$.data.qualityLevel").value("良好"))
                .andExpect(jsonPath("$.data.completenessScore").value(90.0))
                .andExpect(jsonPath("$.data.standardizationScore").value(80.0))
                .andExpect(jsonPath("$.data.executabilityScore").value(85.0));
    }
    
    @Test
    @DisplayName("检查用例完整性-成功")
    void testCheckCompleteness_Success() throws Exception {
        // Given
        Long caseId = 1L;
        TestCase testCase = TestDataBuilder.testCase()
            .withId(caseId)
            .withName("测试用例")
            .withSteps("1. 步骤一\n2. 步骤二")
            .withExpectedResult("预期结果")
            .build();
        
        TestCaseQualityService.CompletenessScore score = new TestCaseQualityService.CompletenessScore();
        score.setTotalScore(90.0);
        score.setPreConditionScore(80.0);
        score.setTestStepScore(100.0);
        score.setExpectedResultScore(100.0);
        score.setBasicInfoScore(80.0);
        
        when(testCaseRepository.findById(caseId))
            .thenReturn(Optional.of(testCase));
        when(qualityService.checkCompleteness(any(TestCase.class)))
            .thenReturn(score);
        
        // When & Then
        mockMvc.perform(get("/v1/test-case-quality/completeness/{caseId}", caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalScore").value(90.0))
                .andExpect(jsonPath("$.data.testStepScore").value(100.0))
                .andExpect(jsonPath("$.data.expectedResultScore").value(100.0));
    }
    
    @Test
    @DisplayName("检查用例规范性-成功")
    void testCheckStandardization_Success() throws Exception {
        // Given
        Long caseId = 1L;
        TestCase testCase = TestDataBuilder.testCase()
            .withId(caseId)
            .withName("投保模块_正常投保流程测试")
            .withSteps("1. 步骤一\n2. 步骤二")
            .withExpectedResult("1. 结果一\n2. 结果二")
            .build();
        
        TestCaseQualityService.StandardizationScore score = new TestCaseQualityService.StandardizationScore();
        score.setTotalScore(85.0);
        score.setNamingScore(90.0);
        score.setFormatScore(85.0);
        score.setContentScore(80.0);
        
        when(testCaseRepository.findById(caseId))
            .thenReturn(Optional.of(testCase));
        when(qualityService.checkStandardization(any(TestCase.class)))
            .thenReturn(score);
        
        // When & Then
        mockMvc.perform(get("/v1/test-case-quality/standardization/{caseId}", caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalScore").value(85.0))
                .andExpect(jsonPath("$.data.namingScore").value(90.0))
                .andExpect(jsonPath("$.data.formatScore").value(85.0))
                .andExpect(jsonPath("$.data.contentScore").value(80.0));
    }
    
    @Test
    @DisplayName("评估用例质量-用例不存在")
    void testAssessQuality_CaseNotFound() throws Exception {
        // Given
        Long caseId = 999L;
        
        when(testCaseRepository.findById(caseId))
            .thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/v1/test-case-quality/assess/{caseId}", caseId))
                .andExpect(status().is5xxServerError());
    }
}

