package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.TestCoverageAnalysisRequestDTO;
import com.sinosoft.testdesign.dto.TestCoverageAnalysisResponseDTO;
import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestCoverageService;
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
 * 测试覆盖分析Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("测试覆盖分析Controller测试")
class TestCoverageControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestCoverageService coverageService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("分析测试覆盖-成功")
    void testAnalyzeCoverage_Success() throws Exception {
        // Given
        TestCoverageAnalysisRequestDTO dto = new TestCoverageAnalysisRequestDTO();
        dto.setAnalysisName("需求覆盖分析");
        dto.setCoverageType("REQUIREMENT");
        dto.setRequirementId(1L);
        
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setId(1L);
        analysis.setAnalysisCode("COV-20240117-001");
        analysis.setCoverageType("REQUIREMENT");
        
        TestCoverageAnalysisResponseDTO responseDTO = new TestCoverageAnalysisResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAnalysisCode("COV-20240117-001");
        responseDTO.setCoverageType("REQUIREMENT");
        
        when(entityDTOMapper.toTestCoverageAnalysisEntity(any(TestCoverageAnalysisRequestDTO.class)))
            .thenReturn(analysis);
        when(coverageService.analyzeCoverage(any(TestCoverageAnalysis.class)))
            .thenReturn(analysis);
        when(entityDTOMapper.toTestCoverageAnalysisResponseDTO(any(TestCoverageAnalysis.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-coverage/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
    
    @Test
    @DisplayName("分析需求覆盖-成功")
    void testAnalyzeRequirementCoverage_Success() throws Exception {
        // Given
        Long requirementId = 1L;
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setId(1L);
        analysis.setAnalysisCode("COV-20240117-001");
        
        TestCoverageAnalysisResponseDTO responseDTO = new TestCoverageAnalysisResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAnalysisCode("COV-20240117-001");
        
        when(coverageService.analyzeRequirementCoverage(requirementId))
            .thenReturn(analysis);
        when(entityDTOMapper.toTestCoverageAnalysisResponseDTO(any(TestCoverageAnalysis.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-coverage/analyze/requirement/{requirementId}", requirementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
    
    @Test
    @DisplayName("查询覆盖分析列表-成功")
    void testGetAnalysisList_Success() throws Exception {
        // Given
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setId(1L);
        analysis.setAnalysisCode("COV-20240117-001");
        
        List<TestCoverageAnalysis> analyses = new ArrayList<>();
        analyses.add(analysis);
        Page<TestCoverageAnalysis> page = new PageImpl<>(analyses, PageRequest.of(0, 10), 1);
        
        TestCoverageAnalysisResponseDTO responseDTO = new TestCoverageAnalysisResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAnalysisCode("COV-20240117-001");
        
        when(coverageService.getAnalysisList(any()))
            .thenReturn(page);
        when(entityDTOMapper.toTestCoverageAnalysisResponseDTO(any(TestCoverageAnalysis.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-coverage")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取分析详情-成功")
    void testGetAnalysisById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setId(id);
        analysis.setAnalysisCode("COV-20240117-001");
        
        TestCoverageAnalysisResponseDTO responseDTO = new TestCoverageAnalysisResponseDTO();
        responseDTO.setId(id);
        responseDTO.setAnalysisCode("COV-20240117-001");
        
        when(coverageService.getAnalysisById(id))
            .thenReturn(analysis);
        when(entityDTOMapper.toTestCoverageAnalysisResponseDTO(any(TestCoverageAnalysis.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-coverage/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
}

