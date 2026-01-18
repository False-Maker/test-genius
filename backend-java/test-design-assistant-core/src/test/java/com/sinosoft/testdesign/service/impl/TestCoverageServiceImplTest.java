package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.repository.TestCoverageAnalysisRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试覆盖分析服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试覆盖分析服务测试")
class TestCoverageServiceImplTest {
    
    @Mock
    private TestCoverageAnalysisRepository analysisRepository;
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @Mock
    private TestCaseRepository testCaseRepository;
    
    @InjectMocks
    private TestCoverageServiceImpl coverageService;
    
    private TestCoverageAnalysis testAnalysis;
    private TestRequirement testRequirement;
    private TestCase testCase;
    
    @BeforeEach
    void setUp() {
        testAnalysis = new TestCoverageAnalysis();
        testAnalysis.setId(1L);
        testAnalysis.setAnalysisCode("COV-20240117-001");
        testAnalysis.setAnalysisName("测试覆盖分析");
        testAnalysis.setRequirementId(1L);
        testAnalysis.setCoverageType("REQUIREMENT");
        testAnalysis.setTotalItems(10);
        testAnalysis.setCoveredItems(8);
        testAnalysis.setCoverageRate(new BigDecimal("80.00"));
        
        testRequirement = new TestRequirement();
        testRequirement.setId(1L);
        testRequirement.setRequirementCode("REQ-20240117-001");
        testRequirement.setRequirementName("测试需求");
        
        testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseCode("CASE-20240117-001");
        testCase.setCaseName("测试用例");
        testCase.setRequirementId(1L);
    }
    
    @Test
    @DisplayName("分析测试覆盖-成功")
    void testAnalyzeCoverage_Success() {
        // Given
        TestCoverageAnalysis newAnalysis = new TestCoverageAnalysis();
        newAnalysis.setAnalysisName("新覆盖分析");
        newAnalysis.setCoverageType("REQUIREMENT");
        newAnalysis.setRequirementId(1L);
        
        TestRequirement requirement = new TestRequirement();
        requirement.setId(1L);
        requirement.setRequirementCode("REQ-20240117-001");
        
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(requirement));
        when(testCaseRepository.findByRequirementId(1L))
            .thenReturn(new ArrayList<>());
        when(analysisRepository.findByAnalysisCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(analysisRepository.save(any(TestCoverageAnalysis.class)))
            .thenAnswer(invocation -> {
                TestCoverageAnalysis analysis = invocation.getArgument(0);
                analysis.setId(1L);
                return analysis;
            });
        
        // When
        TestCoverageAnalysis result = coverageService.analyzeCoverage(newAnalysis);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getAnalysisCode());
        assertTrue(result.getAnalysisCode().startsWith("COV-"));
        verify(analysisRepository, times(1)).save(any(TestCoverageAnalysis.class));
    }
    
    @Test
    @DisplayName("分析需求覆盖-成功")
    void testAnalyzeRequirementCoverage_Success() {
        // Given
        List<TestCase> cases = new ArrayList<>();
        cases.add(testCase);
        
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(testRequirement));
        when(testCaseRepository.findByRequirementId(1L))
            .thenReturn(cases);
        when(analysisRepository.findByAnalysisCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(analysisRepository.save(any(TestCoverageAnalysis.class)))
            .thenAnswer(invocation -> {
                TestCoverageAnalysis analysis = invocation.getArgument(0);
                analysis.setId(1L);
                return analysis;
            });
        
        // When
        TestCoverageAnalysis result = coverageService.analyzeRequirementCoverage(1L);
        
        // Then
        assertNotNull(result);
        assertEquals("REQUIREMENT", result.getCoverageType());
        verify(analysisRepository, times(1)).save(any(TestCoverageAnalysis.class));
    }
    
    @Test
    @DisplayName("查询覆盖分析-根据ID")
    void testGetAnalysisById_Success() {
        // Given
        when(analysisRepository.findById(1L))
            .thenReturn(Optional.of(testAnalysis));
        
        // When
        TestCoverageAnalysis result = coverageService.getAnalysisById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("COV-20240117-001", result.getAnalysisCode());
    }
    
    @Test
    @DisplayName("查询覆盖分析-根据编码")
    void testGetAnalysisByCode_Success() {
        // Given
        when(analysisRepository.findByAnalysisCode("COV-20240117-001"))
            .thenReturn(Optional.of(testAnalysis));
        
        // When
        TestCoverageAnalysis result = coverageService.getAnalysisByCode("COV-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("COV-20240117-001", result.getAnalysisCode());
    }
    
    @Test
    @DisplayName("分页查询覆盖分析列表")
    void testGetAnalysisList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestCoverageAnalysis> analyses = new ArrayList<>();
        analyses.add(testAnalysis);
        Page<TestCoverageAnalysis> page = new PageImpl<>(analyses, pageable, 1);
        
        when(analysisRepository.findAll(pageable))
            .thenReturn(page);
        
        // When
        Page<TestCoverageAnalysis> result = coverageService.getAnalysisList(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(analysisRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("根据需求ID查询覆盖分析列表")
    void testGetAnalysisByRequirementId_Success() {
        // Given
        List<TestCoverageAnalysis> analyses = new ArrayList<>();
        analyses.add(testAnalysis);
        
        when(analysisRepository.findByRequirementId(1L))
            .thenReturn(analyses);
        
        // When
        List<TestCoverageAnalysis> result = coverageService.getAnalysisByRequirementId(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getRequirementId());
    }
    
    @Test
    @DisplayName("检查覆盖不足-成功")
    void testCheckCoverageInsufficiency_Success() {
        // Given
        List<TestCoverageAnalysis> analyses = new ArrayList<>();
        testAnalysis.setCoverageRate(new BigDecimal("60.00")); // 低于阈值
        analyses.add(testAnalysis);
        
        when(analysisRepository.findByRequirementId(1L))
            .thenReturn(analyses);
        
        // When
        String result = coverageService.checkCoverageInsufficiency(1L, 80.0);
        
        // Then
        assertNotNull(result);
        verify(analysisRepository, times(1)).findByRequirementId(1L);
    }
    
    @Test
    @DisplayName("生成覆盖报告-成功")
    void testGenerateCoverageReport_Success() {
        // Given
        List<TestCoverageAnalysis> analyses = new ArrayList<>();
        analyses.add(testAnalysis);
        
        when(analysisRepository.findByRequirementId(1L))
            .thenReturn(analyses);
        
        // When
        String result = coverageService.generateCoverageReport(1L, null);
        
        // Then
        assertNotNull(result);
        verify(analysisRepository, times(1)).findByRequirementId(1L);
    }
}

