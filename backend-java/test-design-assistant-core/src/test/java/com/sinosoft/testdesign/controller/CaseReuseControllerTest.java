package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.service.CaseReuseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用例复用管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("用例复用管理Controller测试")
class CaseReuseControllerTest extends BaseControllerTest {
    
    @MockBean
    private CaseReuseService caseReuseService;
    
    @Test
    @DisplayName("初始化用例向量表-成功")
    void testInitCaseVectorTable_Success() throws Exception {
        // Given
        when(caseReuseService.initCaseVectorTable())
            .thenReturn(true);
        
        // When & Then
        mockMvc.perform(post("/v1/case-reuse/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    @DisplayName("更新用例的向量表示-成功")
    void testUpdateCaseEmbedding_Success() throws Exception {
        // Given
        Long caseId = 1L;
        
        when(caseReuseService.updateCaseEmbedding(caseId))
            .thenReturn(true);
        
        // When & Then
        mockMvc.perform(post("/v1/case-reuse/cases/{caseId}/embedding", caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    @DisplayName("搜索相似用例-成功")
    void testSearchSimilarCases_Success() throws Exception {
        // Given
        String caseText = "测试用例描述";
        int topK = 10;
        double similarityThreshold = 0.7;
        
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> case1 = new HashMap<>();
        case1.put("caseId", 1L);
        case1.put("caseName", "相似用例1");
        case1.put("similarity", 0.85);
        cases.add(case1);
        
        when(caseReuseService.searchSimilarCases(
            eq(caseText), any(), any(), eq(topK), eq(similarityThreshold)))
            .thenReturn(cases);
        
        // When & Then
        mockMvc.perform(post("/v1/case-reuse/cases/search/similar")
                .param("caseText", caseText)
                .param("topK", String.valueOf(topK))
                .param("similarityThreshold", String.valueOf(similarityThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].caseId").value(1L))
                .andExpect(jsonPath("$.data[0].caseName").value("相似用例1"));
    }
    
    @Test
    @DisplayName("搜索相似用例-带过滤条件")
    void testSearchSimilarCases_WithFilters() throws Exception {
        // Given
        String caseText = "测试用例描述";
        Long layerId = 1L;
        Long methodId = 2L;
        int topK = 5;
        
        List<Map<String, Object>> cases = new ArrayList<>();
        
        when(caseReuseService.searchSimilarCases(
            eq(caseText), eq(layerId), eq(methodId), eq(topK), anyDouble()))
            .thenReturn(cases);
        
        // When & Then
        mockMvc.perform(post("/v1/case-reuse/cases/search/similar")
                .param("caseText", caseText)
                .param("layerId", String.valueOf(layerId))
                .param("methodId", String.valueOf(methodId))
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("关键词检索用例-成功")
    void testSearchCasesByKeyword_Success() throws Exception {
        // Given
        String keyword = "投保";
        int topK = 10;
        
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> case1 = new HashMap<>();
        case1.put("caseId", 1L);
        case1.put("caseName", "投保用例");
        cases.add(case1);
        
        when(caseReuseService.searchCasesByKeyword(
            eq(keyword), any(), any(), eq(topK)))
            .thenReturn(cases);
        
        // When & Then
        mockMvc.perform(get("/v1/case-reuse/cases/search/keyword/{keyword}", keyword)
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].caseId").value(1L))
                .andExpect(jsonPath("$.data[0].caseName").value("投保用例"));
    }
    
    @Test
    @DisplayName("关键词检索用例-带过滤条件")
    void testSearchCasesByKeyword_WithFilters() throws Exception {
        // Given
        String keyword = "测试";
        Long layerId = 1L;
        Long methodId = 2L;
        int topK = 5;
        
        List<Map<String, Object>> cases = new ArrayList<>();
        
        when(caseReuseService.searchCasesByKeyword(
            eq(keyword), eq(layerId), eq(methodId), eq(topK)))
            .thenReturn(cases);
        
        // When & Then
        mockMvc.perform(get("/v1/case-reuse/cases/search/keyword/{keyword}", keyword)
                .param("layerId", String.valueOf(layerId))
                .param("methodId", String.valueOf(methodId))
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("推荐相似用例-成功")
    void testRecommendSimilarCases_Success() throws Exception {
        // Given
        Long caseId = 1L;
        int topK = 5;
        
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> case1 = new HashMap<>();
        case1.put("caseId", 2L);
        case1.put("caseName", "推荐用例1");
        case1.put("similarity", 0.9);
        cases.add(case1);
        
        when(caseReuseService.recommendSimilarCases(caseId, topK))
            .thenReturn(cases);
        
        // When & Then
        mockMvc.perform(get("/v1/case-reuse/cases/{caseId}/recommend", caseId)
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].caseId").value(2L))
                .andExpect(jsonPath("$.data[0].caseName").value("推荐用例1"));
    }
    
    @Test
    @DisplayName("创建用例组合-成功")
    void testCreateCaseSuite_Success() throws Exception {
        // Given
        String suiteName = "测试套件1";
        List<Long> caseIds = new ArrayList<>();
        caseIds.add(1L);
        caseIds.add(2L);
        caseIds.add(3L);
        Long suiteId = 100L;
        
        when(caseReuseService.createCaseSuite(
            eq(suiteName), anyList(), any()))
            .thenReturn(suiteId);
        
        // When & Then
        mockMvc.perform(post("/v1/case-reuse/suites")
                .param("suiteName", suiteName)
                .param("caseIds", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(suiteId));
    }
    
    @Test
    @DisplayName("创建用例组合-带创建者ID")
    void testCreateCaseSuite_WithCreatorId() throws Exception {
        // Given
        String suiteName = "测试套件1";
        List<Long> caseIds = new ArrayList<>();
        caseIds.add(1L);
        caseIds.add(2L);
        Long creatorId = 100L;
        Long suiteId = 100L;
        
        when(caseReuseService.createCaseSuite(
            eq(suiteName), anyList(), eq(creatorId)))
            .thenReturn(suiteId);
        
        // When & Then
        mockMvc.perform(post("/v1/case-reuse/suites")
                .param("suiteName", suiteName)
                .param("caseIds", "1", "2")
                .param("creatorId", String.valueOf(creatorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(suiteId));
    }
}

