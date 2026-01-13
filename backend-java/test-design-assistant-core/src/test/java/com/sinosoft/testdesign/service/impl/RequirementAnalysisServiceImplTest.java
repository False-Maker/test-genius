package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.common.TestDataBuilder;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.RequirementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 需求分析服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("需求分析服务测试")
class RequirementAnalysisServiceImplTest {
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private RequirementAnalysisServiceImpl requirementAnalysisService;
    
    private ObjectMapper objectMapper;
    private TestRequirement requirement;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(requirementAnalysisService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(requirementAnalysisService, "aiServiceUrl", "http://localhost:8000");
        
        requirement = TestDataBuilder.requirement()
            .withId(1L)
            .withName("投保需求")
            .withDescription("用户可以通过系统进行投保操作，包括填写投保信息、选择保险产品、提交投保申请等")
            .build();
    }
    
    @Test
    @DisplayName("分析需求-成功")
    void testAnalyzeRequirement_Success() {
        // Given
        Long requirementId = 1L;
        
        when(requirementRepository.findById(requirementId))
            .thenReturn(Optional.of(requirement));
        
        // When
        RequirementAnalysisServiceImpl.RequirementAnalysisResult result = 
            requirementAnalysisService.analyzeRequirement(requirementId);
        
        // Then
        assertNotNull(result);
        assertEquals(requirementId, result.getRequirementId());
        assertEquals("投保需求", result.getRequirementName());
        assertNotNull(result.getRequirementText());
        assertNotNull(result.getTestPoints());
        assertNotNull(result.getBusinessRules());
        assertNotNull(result.getKeyInfo());
        verify(requirementRepository, times(1)).findById(requirementId);
    }
    
    @Test
    @DisplayName("分析需求-需求不存在")
    void testAnalyzeRequirement_NotFound() {
        // Given
        Long requirementId = 999L;
        
        when(requirementRepository.findById(requirementId))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementAnalysisService.analyzeRequirement(requirementId);
        });
        
        assertTrue(exception.getMessage().contains("需求不存在"));
        verify(requirementRepository, times(1)).findById(requirementId);
    }
    
    @Test
    @DisplayName("分析需求-需求描述为空")
    void testAnalyzeRequirement_EmptyDescription() {
        // Given
        Long requirementId = 1L;
        requirement.setRequirementDescription("");
        requirement.setRequirementDocUrl(null);
        
        when(requirementRepository.findById(requirementId))
            .thenReturn(Optional.of(requirement));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementAnalysisService.analyzeRequirement(requirementId);
        });
        
        assertTrue(exception.getMessage().contains("需求描述或文档内容不能为空"));
    }
    
    @Test
    @DisplayName("分析需求-有文档URL")
    void testAnalyzeRequirement_WithDocument() {
        // Given
        Long requirementId = 1L;
        requirement.setRequirementDocUrl("/path/to/document.docx");
        
        Map<String, Object> documentResponse = new HashMap<>();
        documentResponse.put("content", "这是从文档解析出的内容");
        
        when(requirementRepository.findById(requirementId))
            .thenReturn(Optional.of(requirement));
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
            .thenReturn(new ResponseEntity<>(documentResponse, HttpStatus.OK));
        
        // When
        RequirementAnalysisServiceImpl.RequirementAnalysisResult result = 
            requirementAnalysisService.analyzeRequirement(requirementId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getRequirementText().contains("这是从文档解析出的内容"));
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Map.class));
    }
    
    @Test
    @DisplayName("提取测试要点-成功")
    void testExtractTestPoints_Success() {
        // Given
        Long requirementId = 1L;
        
        when(requirementRepository.findById(requirementId))
            .thenReturn(Optional.of(requirement));
        
        // When
        List<RequirementAnalysisServiceImpl.TestPoint> testPoints = 
            requirementAnalysisService.extractTestPoints(requirementId);
        
        // Then
        assertNotNull(testPoints);
        verify(requirementRepository, times(1)).findById(requirementId);
    }
    
    @Test
    @DisplayName("提取业务规则-成功")
    void testExtractBusinessRules_Success() {
        // Given
        Long requirementId = 1L;
        
        when(requirementRepository.findById(requirementId))
            .thenReturn(Optional.of(requirement));
        
        // When
        List<RequirementAnalysisServiceImpl.BusinessRule> businessRules = 
            requirementAnalysisService.extractBusinessRules(requirementId);
        
        // Then
        assertNotNull(businessRules);
        verify(requirementRepository, times(1)).findById(requirementId);
    }
}

