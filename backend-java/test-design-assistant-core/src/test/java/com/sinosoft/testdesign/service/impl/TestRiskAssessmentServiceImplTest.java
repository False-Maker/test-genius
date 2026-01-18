package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestRiskAssessment;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import com.sinosoft.testdesign.repository.TestRiskAssessmentRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.repository.TestExecutionTaskRepository;
import com.sinosoft.testdesign.repository.TestExecutionRecordRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.TestCoverageAnalysisRepository;
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
 * 风险评估服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("风险评估服务测试")
class TestRiskAssessmentServiceImplTest {
    
    @Mock
    private TestRiskAssessmentRepository assessmentRepository;
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @Mock
    private TestExecutionTaskRepository taskRepository;
    
    @Mock
    private TestExecutionRecordRepository recordRepository;
    
    @Mock
    private TestCaseRepository testCaseRepository;
    
    @Mock
    private TestCoverageAnalysisRepository coverageRepository;
    
    @InjectMocks
    private TestRiskAssessmentServiceImpl riskService;
    
    private TestRiskAssessment testAssessment;
    private TestRequirement testRequirement;
    private TestExecutionTask testTask;
    private TestExecutionRecord testRecord;
    
    @BeforeEach
    void setUp() {
        testAssessment = new TestRiskAssessment();
        testAssessment.setId(1L);
        testAssessment.setAssessmentCode("RISK-20240117-001");
        testAssessment.setAssessmentName("风险评估");
        testAssessment.setRequirementId(1L);
        testAssessment.setRiskLevel("MEDIUM");
        testAssessment.setRiskScore(new BigDecimal("60.00"));
        testAssessment.setFeasibilityScore(new BigDecimal("70.00"));
        
        testRequirement = new TestRequirement();
        testRequirement.setId(1L);
        testRequirement.setRequirementCode("REQ-20240117-001");
        testRequirement.setRequirementName("测试需求");
        
        testTask = new TestExecutionTask();
        testTask.setId(1L);
        testTask.setTaskCode("TASK-20240117-001");
        testTask.setTaskStatus("SUCCESS");
        
        testRecord = new TestExecutionRecord();
        testRecord.setId(1L);
        testRecord.setTaskId(1L);
        testRecord.setExecutionStatus("SUCCESS");
    }
    
    @Test
    @DisplayName("执行风险评估-成功")
    void testAssessRisk_Success() {
        // Given
        TestRiskAssessment newAssessment = new TestRiskAssessment();
        newAssessment.setAssessmentName("新风险评估");
        newAssessment.setRequirementId(1L);
        
        List<TestCase> cases = new ArrayList<>();
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        cases.add(testCase);
        
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(testRequirement));
        when(testCaseRepository.findByRequirementId(1L))
            .thenReturn(cases);
        when(coverageRepository.findByRequirementId(1L))
            .thenReturn(new ArrayList<>());
        when(assessmentRepository.findByAssessmentCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(assessmentRepository.save(any(TestRiskAssessment.class)))
            .thenAnswer(invocation -> {
                TestRiskAssessment assessment = invocation.getArgument(0);
                assessment.setId(1L);
                return assessment;
            });
        // recordRepository.findAll在assessRisk中不会被调用，因为executionTaskId为null
        // 只有在assessExecutionRisk时才会调用
        
        // When
        TestRiskAssessment result = riskService.assessRisk(newAssessment);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getAssessmentCode());
        assertTrue(result.getAssessmentCode().startsWith("RISK-"));
        verify(assessmentRepository, times(1)).save(any(TestRiskAssessment.class));
    }
    
    @Test
    @DisplayName("评估需求风险-成功")
    void testAssessRequirementRisk_Success() {
        // Given
        List<TestCase> cases = new ArrayList<>();
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        cases.add(testCase);
        
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(testRequirement));
        when(testCaseRepository.findByRequirementId(1L))
            .thenReturn(cases);
        when(coverageRepository.findByRequirementId(1L))
            .thenReturn(new ArrayList<>());
        when(assessmentRepository.findByAssessmentCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(assessmentRepository.save(any(TestRiskAssessment.class)))
            .thenAnswer(invocation -> {
                TestRiskAssessment assessment = invocation.getArgument(0);
                assessment.setId(1L);
                return assessment;
            });
        
        // When
        TestRiskAssessment result = riskService.assessRequirementRisk(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRequirementId());
        verify(assessmentRepository, times(1)).save(any(TestRiskAssessment.class));
    }
    
    @Test
    @DisplayName("评估执行任务风险-成功")
    void testAssessExecutionTaskRisk_Success() {
        // Given
        List<TestExecutionRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        when(recordRepository.findByTaskId(1L))
            .thenReturn(records);
        when(assessmentRepository.findByAssessmentCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(assessmentRepository.save(any(TestRiskAssessment.class)))
            .thenAnswer(invocation -> {
                TestRiskAssessment assessment = invocation.getArgument(0);
                assessment.setId(1L);
                return assessment;
            });
        
        // When
        TestRiskAssessment result = riskService.assessExecutionTaskRisk(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getExecutionTaskId());
        verify(assessmentRepository, times(1)).save(any(TestRiskAssessment.class));
    }
    
    @Test
    @DisplayName("评估风险等级-高风险")
    void testAssessRiskLevel_High() {
        // When
        String result = riskService.assessRiskLevel(new BigDecimal("85.00"));
        
        // Then
        assertEquals("HIGH", result);
    }
    
    @Test
    @DisplayName("评估风险等级-中风险")
    void testAssessRiskLevel_Medium() {
        // When
        String result = riskService.assessRiskLevel(new BigDecimal("50.00"));
        
        // Then
        assertEquals("MEDIUM", result);
    }
    
    @Test
    @DisplayName("评估风险等级-低风险")
    void testAssessRiskLevel_Low() {
        // When
        String result = riskService.assessRiskLevel(new BigDecimal("30.00"));
        
        // Then
        assertEquals("LOW", result);
    }
    
    @Test
    @DisplayName("评估上线可行性-成功")
    void testAssessFeasibility_Success() {
        // Given
        List<TestExecutionRecord> records = new ArrayList<>();
        testRecord.setExecutionStatus("SUCCESS");
        records.add(testRecord);
        
        List<TestCase> cases = new ArrayList<>();
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(testRequirement));
        when(testCaseRepository.findByRequirementId(1L))
            .thenReturn(cases);
        when(coverageRepository.findByRequirementId(1L))
            .thenReturn(new ArrayList<>());
        
        // When
        BigDecimal result = riskService.assessFeasibility(1L, null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(result.compareTo(new BigDecimal("100")) <= 0);
    }
    
    @Test
    @DisplayName("查询风险评估-根据ID")
    void testGetAssessmentById_Success() {
        // Given
        when(assessmentRepository.findById(1L))
            .thenReturn(Optional.of(testAssessment));
        
        // When
        TestRiskAssessment result = riskService.getAssessmentById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("RISK-20240117-001", result.getAssessmentCode());
    }
    
    @Test
    @DisplayName("查询风险评估-根据编码")
    void testGetAssessmentByCode_Success() {
        // Given
        when(assessmentRepository.findByAssessmentCode("RISK-20240117-001"))
            .thenReturn(Optional.of(testAssessment));
        
        // When
        TestRiskAssessment result = riskService.getAssessmentByCode("RISK-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("RISK-20240117-001", result.getAssessmentCode());
    }
    
    @Test
    @DisplayName("分页查询风险评估列表")
    void testGetAssessmentList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestRiskAssessment> assessments = new ArrayList<>();
        assessments.add(testAssessment);
        Page<TestRiskAssessment> page = new PageImpl<>(assessments, pageable, 1);
        
        when(assessmentRepository.findAll(pageable))
            .thenReturn(page);
        
        // When
        Page<TestRiskAssessment> result = riskService.getAssessmentList(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(assessmentRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("识别风险项-成功")
    void testIdentifyRiskItems_Success() {
        // Given
        List<TestExecutionRecord> records = new ArrayList<>();
        testRecord.setExecutionStatus("FAILED");
        records.add(testRecord);
        
        List<TestCase> cases = new ArrayList<>();
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(testRequirement));
        when(testCaseRepository.findByRequirementId(1L))
            .thenReturn(cases);
        when(coverageRepository.findByRequirementId(1L))
            .thenReturn(new ArrayList<>());
        
        // When
        String result = riskService.identifyRiskItems(1L, null);
        
        // Then
        assertNotNull(result);
        // identifyRiskItems方法会检查用例数量和覆盖率，不会调用recordRepository
        verify(testCaseRepository, times(1)).findByRequirementId(1L);
        verify(coverageRepository, times(1)).findByRequirementId(1L);
        verify(recordRepository, never()).findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }
}

