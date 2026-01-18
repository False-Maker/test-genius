package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.BatchCaseGenerationRequest;
import com.sinosoft.testdesign.dto.CaseGenerationRequest;
import com.sinosoft.testdesign.dto.CaseGenerationResult;
import com.sinosoft.testdesign.dto.GenerationTaskDTO;
import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.metrics.BusinessMetricsCollector;
import com.sinosoft.testdesign.repository.*;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import com.sinosoft.testdesign.service.TestCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 智能用例生成服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("智能用例生成服务测试")
class IntelligentCaseGenerationServiceImplTest {
    
    @Mock
    private CaseGenerationTaskRepository taskRepository;
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @Mock
    private TestLayerRepository layerRepository;
    
    @Mock
    private TestMethodRepository methodRepository;
    
    @Mock
    private ModelConfigRepository modelConfigRepository;
    
    @Mock
    private TestCaseRepository testCaseRepository;
    
    @Mock
    private TestCaseService testCaseService;
    
    @Mock
    private SpecificationCheckService specificationCheckService;
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private BusinessMetricsCollector metricsCollector;
    
    @InjectMocks
    private IntelligentCaseGenerationServiceImpl intelligentCaseGenerationService;
    
    private ObjectMapper objectMapper;
    private TestRequirement testRequirement;
    private TestLayer testLayer;
    private TestDesignMethod testMethod;
    private ModelConfig modelConfig;
    private CaseGenerationTask task;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(intelligentCaseGenerationService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(intelligentCaseGenerationService, "aiServiceUrl", "http://localhost:8000");
        
        // 初始化测试数据
        testRequirement = new TestRequirement();
        testRequirement.setId(1L);
        testRequirement.setRequirementCode("REQ-20240101-001");
        testRequirement.setRequirementName("测试需求");
        testRequirement.setRequirementDescription("这是一个测试需求描述");
        
        testLayer = new TestLayer();
        testLayer.setId(1L);
        testLayer.setLayerCode("UNIT");
        testLayer.setLayerName("单元测试");
        
        testMethod = new TestDesignMethod();
        testMethod.setId(1L);
        testMethod.setMethodCode("EQUIVALENCE");
        testMethod.setMethodName("等价类划分");
        
        modelConfig = new ModelConfig();
        modelConfig.setId(1L);
        modelConfig.setModelCode("DEEPSEEK");
        modelConfig.setModelName("DeepSeek");
        modelConfig.setIsActive("1");
        modelConfig.setPriority(1);
        
        task = new CaseGenerationTask();
        task.setId(1L);
        task.setTaskCode("TASK-20240101-001");
        task.setRequirementId(1L);
        task.setLayerId(1L);
        task.setMethodId(1L);
        task.setModelCode("DEEPSEEK");
        task.setTaskStatus("PENDING");
        task.setProgress(0);
        task.setCreateTime(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("生成测试用例-成功")
    void testGenerateTestCases_Success() {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(1L);
        request.setLayerCode("UNIT");
        request.setMethodCode("EQUIVALENCE");
        request.setModelCode("DEEPSEEK");
        request.setCreatorId(1L);
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(layerRepository.findByLayerCode("UNIT")).thenReturn(Optional.of(testLayer));
        when(methodRepository.findByMethodCode("EQUIVALENCE")).thenReturn(Optional.of(testMethod));
        when(modelConfigRepository.findByModelCode("DEEPSEEK")).thenReturn(Optional.of(modelConfig));
        when(taskRepository.findByTaskCodeStartingWithOrderByIdDesc(anyString())).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(CaseGenerationTask.class))).thenAnswer(invocation -> {
            CaseGenerationTask t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        // When
        CaseGenerationResult result = intelligentCaseGenerationService.generateTestCases(request);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTaskId());
        assertEquals("PROCESSING", result.getStatus());
        assertNotNull(result.getMessage());
        verify(taskRepository, times(1)).save(any(CaseGenerationTask.class));
    }
    
    @Test
    @DisplayName("生成测试用例-需求不存在")
    void testGenerateTestCases_RequirementNotFound() {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(999L);
        
        when(requirementRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.generateTestCases(request);
        });
        
        assertEquals("需求不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("生成测试用例-测试分层不存在")
    void testGenerateTestCases_LayerNotFound() {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(1L);
        request.setLayerCode("INVALID");
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(layerRepository.findByLayerCode("INVALID")).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.generateTestCases(request);
        });
        
        assertTrue(exception.getMessage().contains("测试分层不存在"));
    }
    
    @Test
    @DisplayName("生成测试用例-测试方法不存在")
    void testGenerateTestCases_MethodNotFound() {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(1L);
        request.setLayerCode("UNIT");
        request.setMethodCode("INVALID");
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(layerRepository.findByLayerCode("UNIT")).thenReturn(Optional.of(testLayer));
        when(methodRepository.findByMethodCode("INVALID")).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.generateTestCases(request);
        });
        
        assertTrue(exception.getMessage().contains("测试方法不存在"));
    }
    
    @Test
    @DisplayName("生成测试用例-使用默认模型")
    void testGenerateTestCases_UseDefaultModel() {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(1L);
        request.setLayerCode("UNIT");
        request.setMethodCode("EQUIVALENCE");
        request.setModelCode(null); // 不指定模型
        request.setCreatorId(1L);
        
        List<ModelConfig> activeModels = new ArrayList<>();
        activeModels.add(modelConfig);
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(layerRepository.findByLayerCode("UNIT")).thenReturn(Optional.of(testLayer));
        when(methodRepository.findByMethodCode("EQUIVALENCE")).thenReturn(Optional.of(testMethod));
        when(modelConfigRepository.findByIsActiveOrderByPriorityAsc("1")).thenReturn(activeModels);
        when(taskRepository.findByTaskCodeStartingWithOrderByIdDesc(anyString())).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(CaseGenerationTask.class))).thenAnswer(invocation -> {
            CaseGenerationTask t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        // When
        CaseGenerationResult result = intelligentCaseGenerationService.generateTestCases(request);
        
        // Then
        assertNotNull(result);
        verify(modelConfigRepository, times(1)).findByIsActiveOrderByPriorityAsc("1");
    }
    
    @Test
    @DisplayName("生成测试用例-没有可用模型")
    void testGenerateTestCases_NoAvailableModel() {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(1L);
        request.setLayerCode("UNIT");
        request.setMethodCode("EQUIVALENCE");
        request.setModelCode(null);
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(layerRepository.findByLayerCode("UNIT")).thenReturn(Optional.of(testLayer));
        when(methodRepository.findByMethodCode("EQUIVALENCE")).thenReturn(Optional.of(testMethod));
        when(modelConfigRepository.findByIsActiveOrderByPriorityAsc("1")).thenReturn(new ArrayList<>());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.generateTestCases(request);
        });
        
        assertEquals("没有可用的模型配置", exception.getMessage());
    }
    
    @Test
    @DisplayName("查询生成任务-成功")
    void testGetGenerationTask_Success() {
        // Given
        task.setTaskStatus("SUCCESS");
        task.setProgress(100);
        task.setTotalCases(5);
        task.setSuccessCases(5);
        task.setFailCases(0);
        task.setResultData("[]");
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        // When
        GenerationTaskDTO result = intelligentCaseGenerationService.getGenerationTask(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TASK-20240101-001", result.getTaskCode());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(100, result.getProgress());
        assertEquals(5, result.getTotalCases());
    }
    
    @Test
    @DisplayName("查询生成任务-任务不存在")
    void testGetGenerationTask_NotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.getGenerationTask(999L);
        });
        
        assertEquals("任务不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("批量查询生成任务-成功")
    void testGetBatchGenerationTasks_Success() {
        // Given
        List<Long> taskIds = List.of(1L, 2L);
        CaseGenerationTask task2 = new CaseGenerationTask();
        task2.setId(2L);
        task2.setTaskCode("TASK-20240101-002");
        task2.setTaskStatus("PROCESSING");
        
        when(taskRepository.findAllById(taskIds)).thenReturn(List.of(task, task2));
        
        // When
        List<GenerationTaskDTO> result = intelligentCaseGenerationService.getBatchGenerationTasks(taskIds);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }
    
    @Test
    @DisplayName("批量查询生成任务-空列表")
    void testGetBatchGenerationTasks_EmptyList() {
        // Given
        List<Long> taskIds = new ArrayList<>();
        
        // When
        List<GenerationTaskDTO> result = intelligentCaseGenerationService.getBatchGenerationTasks(taskIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, never()).findAllById(any());
    }
    
    @Test
    @DisplayName("批量查询生成任务-null列表")
    void testGetBatchGenerationTasks_NullList() {
        // When
        List<GenerationTaskDTO> result = intelligentCaseGenerationService.getBatchGenerationTasks(null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("批量生成测试用例-成功")
    void testBatchGenerateTestCases_Success() {
        // Given
        BatchCaseGenerationRequest request = new BatchCaseGenerationRequest();
        request.setRequirementIds(List.of(1L, 2L));
        request.setLayerCode("UNIT");
        request.setMethodCode("EQUIVALENCE");
        request.setModelCode("DEEPSEEK");
        request.setCreatorId(1L);
        
        TestRequirement req2 = new TestRequirement();
        req2.setId(2L);
        req2.setRequirementCode("REQ-20240101-002");
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(requirementRepository.findById(2L)).thenReturn(Optional.of(req2));
        when(layerRepository.findByLayerCode("UNIT")).thenReturn(Optional.of(testLayer));
        when(methodRepository.findByMethodCode("EQUIVALENCE")).thenReturn(Optional.of(testMethod));
        when(modelConfigRepository.findByModelCode("DEEPSEEK")).thenReturn(Optional.of(modelConfig));
        when(taskRepository.findByTaskCodeStartingWithOrderByIdDesc(anyString())).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(CaseGenerationTask.class))).thenAnswer(invocation -> {
            CaseGenerationTask t = invocation.getArgument(0);
            t.setId(invocation.getArgument(0, CaseGenerationTask.class).getRequirementId());
            return t;
        });
        
        // When
        var result = intelligentCaseGenerationService.batchGenerateTestCases(request);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalTasks());
        assertEquals(2, result.getSuccessTasks());
        assertEquals(0, result.getFailTasks());
        assertEquals(2, result.getTaskIds().size());
        verify(taskRepository, times(2)).save(any(CaseGenerationTask.class));
    }
    
    @Test
    @DisplayName("批量生成测试用例-需求ID列表为空")
    void testBatchGenerateTestCases_EmptyRequirementIds() {
        // Given
        BatchCaseGenerationRequest request = new BatchCaseGenerationRequest();
        request.setRequirementIds(new ArrayList<>());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.batchGenerateTestCases(request);
        });
        
        assertEquals("需求ID列表不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("批量生成测试用例-需求ID列表为null")
    void testBatchGenerateTestCases_NullRequirementIds() {
        // Given
        BatchCaseGenerationRequest request = new BatchCaseGenerationRequest();
        request.setRequirementIds(null);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.batchGenerateTestCases(request);
        });
        
        assertEquals("需求ID列表不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("批量生成测试用例-部分需求不存在")
    void testBatchGenerateTestCases_PartialFailure() {
        // Given
        BatchCaseGenerationRequest request = new BatchCaseGenerationRequest();
        request.setRequirementIds(List.of(1L, 999L));
        request.setLayerCode("UNIT");
        request.setMethodCode("EQUIVALENCE");
        request.setModelCode("DEEPSEEK");
        request.setCreatorId(1L);
        
        when(requirementRepository.findById(1L)).thenReturn(Optional.of(testRequirement));
        when(requirementRepository.findById(999L)).thenReturn(Optional.empty());
        when(layerRepository.findByLayerCode("UNIT")).thenReturn(Optional.of(testLayer));
        when(methodRepository.findByMethodCode("EQUIVALENCE")).thenReturn(Optional.of(testMethod));
        when(modelConfigRepository.findByModelCode("DEEPSEEK")).thenReturn(Optional.of(modelConfig));
        when(taskRepository.findByTaskCodeStartingWithOrderByIdDesc(anyString())).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(CaseGenerationTask.class))).thenAnswer(invocation -> {
            CaseGenerationTask t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        // When
        var result = intelligentCaseGenerationService.batchGenerateTestCases(request);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalTasks());
        assertEquals(1, result.getSuccessTasks());
        assertEquals(1, result.getFailTasks());
        assertEquals(1, result.getTaskIds().size());
    }
}

