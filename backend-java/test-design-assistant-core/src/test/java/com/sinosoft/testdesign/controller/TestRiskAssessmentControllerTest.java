package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.TestRiskAssessmentRequestDTO;
import com.sinosoft.testdesign.dto.TestRiskAssessmentResponseDTO;
import com.sinosoft.testdesign.entity.TestRiskAssessment;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestRiskAssessmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 风险评估Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("风险评估Controller测试")
class TestRiskAssessmentControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestRiskAssessmentService riskService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("执行风险评估-成功")
    void testAssessRisk_Success() throws Exception {
        // Given
        TestRiskAssessmentRequestDTO dto = new TestRiskAssessmentRequestDTO();
        dto.setRequirementId(1L);
        dto.setAssessmentName("需求风险评估");
        
        TestRiskAssessment assessment = new TestRiskAssessment();
        assessment.setId(1L);
        assessment.setAssessmentCode("RISK-20240117-001");
        assessment.setRiskScore(BigDecimal.valueOf(75.5));
        
        TestRiskAssessmentResponseDTO responseDTO = new TestRiskAssessmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAssessmentCode("RISK-20240117-001");
        responseDTO.setRiskScore(BigDecimal.valueOf(75.5));
        
        when(entityDTOMapper.toTestRiskAssessmentEntity(any(TestRiskAssessmentRequestDTO.class)))
            .thenReturn(assessment);
        when(riskService.assessRisk(any(TestRiskAssessment.class)))
            .thenReturn(assessment);
        when(entityDTOMapper.toTestRiskAssessmentResponseDTO(any(TestRiskAssessment.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-risk-assessment/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
    
    @Test
    @DisplayName("评估需求风险-成功")
    void testAssessRequirementRisk_Success() throws Exception {
        // Given
        Long requirementId = 1L;
        TestRiskAssessment assessment = new TestRiskAssessment();
        assessment.setId(1L);
        assessment.setAssessmentCode("RISK-20240117-001");
        
        TestRiskAssessmentResponseDTO responseDTO = new TestRiskAssessmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAssessmentCode("RISK-20240117-001");
        
        when(riskService.assessRequirementRisk(requirementId))
            .thenReturn(assessment);
        when(entityDTOMapper.toTestRiskAssessmentResponseDTO(any(TestRiskAssessment.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-risk-assessment/assess/requirement/{requirementId}", requirementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
    
    @Test
    @DisplayName("评估风险等级-成功")
    void testAssessRiskLevel_Success() throws Exception {
        // Given
        BigDecimal riskScore = BigDecimal.valueOf(75.5);
        String riskLevel = "MEDIUM";
        
        when(riskService.assessRiskLevel(riskScore))
            .thenReturn(riskLevel);
        
        // When & Then
        mockMvc.perform(get("/v1/test-risk-assessment/assess/level")
                .param("riskScore", "75.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("MEDIUM"));
    }
    
    @Test
    @DisplayName("查询风险评估列表-成功")
    void testGetAssessmentList_Success() throws Exception {
        // Given
        TestRiskAssessment assessment = new TestRiskAssessment();
        assessment.setId(1L);
        assessment.setAssessmentCode("RISK-20240117-001");
        
        List<TestRiskAssessment> assessments = new ArrayList<>();
        assessments.add(assessment);
        Page<TestRiskAssessment> page = new PageImpl<>(assessments, PageRequest.of(0, 10), 1);
        
        TestRiskAssessmentResponseDTO responseDTO = new TestRiskAssessmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setAssessmentCode("RISK-20240117-001");
        
        when(riskService.getAssessmentList(any()))
            .thenReturn(page);
        when(entityDTOMapper.toTestRiskAssessmentResponseDTO(any(TestRiskAssessment.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-risk-assessment")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}

