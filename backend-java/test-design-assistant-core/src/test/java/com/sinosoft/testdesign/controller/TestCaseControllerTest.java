package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.service.TestCaseImportExportService;
import com.sinosoft.testdesign.service.TestCaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试用例管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("测试用例管理Controller测试")
class TestCaseControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestCaseService testCaseService;
    
    @MockBean
    private TestCaseImportExportService importExportService;
    
    @Test
    @DisplayName("创建用例-成功")
    void testCreateTestCase_Success() throws Exception {
        // Given
        TestCase testCase = new TestCase();
        testCase.setCaseName("测试用例");
        testCase.setTestStep("1. 步骤一\n2. 步骤二");
        testCase.setExpectedResult("预期结果");
        
        TestCase savedCase = new TestCase();
        savedCase.setId(1L);
        savedCase.setCaseCode("CASE-20240101-001");
        savedCase.setCaseName("测试用例");
        savedCase.setTestStep("1. 步骤一\n2. 步骤二");
        savedCase.setExpectedResult("预期结果");
        savedCase.setCaseStatus(CaseStatus.DRAFT.name());
        
        when(testCaseService.createTestCase(any(TestCase.class)))
            .thenReturn(savedCase);
        
        // When & Then
        mockMvc.perform(post("/v1/test-cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.caseCode").value("CASE-20240101-001"))
                .andExpect(jsonPath("$.data.caseName").value("测试用例"));
    }
    
    @Test
    @DisplayName("查询用例-根据ID")
    void testGetTestCaseById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestCase testCase = new TestCase();
        testCase.setId(id);
        testCase.setCaseCode("CASE-20240101-001");
        testCase.setCaseName("测试用例");
        
        when(testCaseService.getTestCaseById(id))
            .thenReturn(testCase);
        
        // When & Then
        mockMvc.perform(get("/v1/test-cases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.caseCode").value("CASE-20240101-001"))
                .andExpect(jsonPath("$.data.caseName").value("测试用例"));
    }
    
    @Test
    @DisplayName("分页查询用例列表-成功")
    void testGetTestCaseList_Success() throws Exception {
        // Given
        List<TestCase> testCases = new ArrayList<>();
        TestCase case1 = new TestCase();
        case1.setId(1L);
        case1.setCaseCode("CASE-20240101-001");
        case1.setCaseName("用例1");
        testCases.add(case1);
        
        Page<TestCase> page = new PageImpl<>(testCases, PageRequest.of(0, 10), 1);
        
        when(testCaseService.getTestCaseList(any(), any(), any(), any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/v1/test-cases")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("分页查询用例列表-按名称搜索")
    void testGetTestCaseList_ByName() throws Exception {
        // Given
        List<TestCase> testCases = new ArrayList<>();
        TestCase case1 = new TestCase();
        case1.setId(1L);
        case1.setCaseCode("CASE-20240101-001");
        case1.setCaseName("投保用例");
        testCases.add(case1);
        
        Page<TestCase> page = new PageImpl<>(testCases, PageRequest.of(0, 10), 1);
        
        when(testCaseService.getTestCaseList(any(), eq("投保"), any(), any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/v1/test-cases")
                .param("page", "0")
                .param("size", "10")
                .param("caseName", "投保"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("更新用例-成功")
    void testUpdateTestCase_Success() throws Exception {
        // Given
        Long id = 1L;
        TestCase testCase = new TestCase();
        testCase.setCaseName("更新后的用例名称");
        
        TestCase updatedCase = new TestCase();
        updatedCase.setId(id);
        updatedCase.setCaseCode("CASE-20240101-001");
        updatedCase.setCaseName("更新后的用例名称");
        
        when(testCaseService.updateTestCase(eq(id), any(TestCase.class)))
            .thenReturn(updatedCase);
        
        // When & Then
        mockMvc.perform(put("/v1/test-cases/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseName").value("更新后的用例名称"));
    }
    
    @Test
    @DisplayName("删除用例-成功")
    void testDeleteTestCase_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/test-cases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    @DisplayName("更新用例状态-成功")
    void testUpdateCaseStatus_Success() throws Exception {
        // Given
        Long id = 1L;
        String newStatus = CaseStatus.PENDING_REVIEW.name();
        
        TestCase updatedCase = new TestCase();
        updatedCase.setId(id);
        updatedCase.setCaseCode("CASE-20240101-001");
        updatedCase.setCaseStatus(newStatus);
        
        when(testCaseService.updateCaseStatus(id, newStatus))
            .thenReturn(updatedCase);
        
        // When & Then
        mockMvc.perform(put("/v1/test-cases/{id}/status", id)
                .param("status", newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseStatus").value(newStatus));
    }
    
    @Test
    @DisplayName("审核用例-成功")
    void testReviewTestCase_Success() throws Exception {
        // Given
        Long id = 1L;
        String reviewResult = "PASS";
        String reviewComment = "审核通过";
        
        TestCase reviewedCase = new TestCase();
        reviewedCase.setId(id);
        reviewedCase.setCaseCode("CASE-20240101-001");
        reviewedCase.setCaseStatus(CaseStatus.REVIEWED.name());
        
        when(testCaseService.reviewTestCase(eq(id), eq(reviewResult), anyString()))
            .thenReturn(reviewedCase);
        
        // When & Then
        mockMvc.perform(post("/v1/test-cases/{id}/review", id)
                .param("reviewResult", reviewResult)
                .param("reviewComment", reviewComment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
}

