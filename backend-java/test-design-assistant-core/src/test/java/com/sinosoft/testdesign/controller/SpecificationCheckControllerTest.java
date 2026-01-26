package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestSpecification;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import com.sinosoft.testdesign.service.TestCaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 规约检查Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("规约检查Controller测试")
class SpecificationCheckControllerTest extends BaseControllerTest {
    
    @MockBean
    private SpecificationCheckService specificationCheckService;
    
    @MockBean
    private TestCaseService testCaseService;
    
    @Test
    @DisplayName("自动匹配适用的规约-成功")
    void testMatchSpecifications_Success() throws Exception {
        // Given
        Long caseId = 1L;
        TestCase testCase = new TestCase();
        testCase.setId(caseId);
        testCase.setCaseCode("CASE-20240117-001");
        
        TestSpecification spec = new TestSpecification();
        spec.setId(1L);
        spec.setSpecCode("SPEC-20240117-001");
        spec.setSpecName("测试规约");
        
        List<TestSpecification> specs = new ArrayList<>();
        specs.add(spec);
        
        when(testCaseService.getTestCaseById(caseId))
            .thenReturn(testCase);
        when(specificationCheckService.matchSpecifications(any(TestCase.class)))
            .thenReturn(specs);
        
        // When & Then
        mockMvc.perform(get("/v1/specification-check/match/{caseId}", caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("检查用例是否符合规约-成功")
    void testCheckCompliance_Success() throws Exception {
        // Given
        SpecificationCheckRequestDTO request = new SpecificationCheckRequestDTO();
        request.setCaseId(1L);
        
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseCode("CASE-20240117-001");
        
        SpecificationCheckService.SpecificationComplianceResult result = 
            new SpecificationCheckService.SpecificationComplianceResult();
        result.setCompliant(true);
        result.setComplianceScore(85.5);
        result.setTotalChecks(10);
        result.setPassedChecks(9);
        result.setFailedChecks(1);
        result.setIssues(new ArrayList<>());
        
        List<TestSpecification> matchedSpecs = new ArrayList<>();
        
        when(testCaseService.getTestCaseById(1L))
            .thenReturn(testCase);
        when(specificationCheckService.checkCompliance(any(TestCase.class), any()))
            .thenReturn(result);
        when(specificationCheckService.matchSpecifications(any(TestCase.class)))
            .thenReturn(matchedSpecs);
        
        // When & Then
        mockMvc.perform(post("/v1/specification-check/check")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isCompliant").value(true));
    }
}

