package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BaseIntegrationTest;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.enums.RequirementStatus;
import com.sinosoft.testdesign.repository.*;
import com.sinosoft.testdesign.service.IntelligentCaseGenerationService;
import com.sinosoft.testdesign.service.TestCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 智能用例生成服务集成测试
 * 测试executeGenerationTask方法的完整执行流程
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("智能用例生成服务集成测试")
class IntelligentCaseGenerationServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private IntelligentCaseGenerationService intelligentCaseGenerationService;
    
    @Autowired
    private CaseGenerationTaskRepository taskRepository;
    
    @Autowired
    private RequirementRepository requirementRepository;
    
    @Autowired
    private TestLayerRepository layerRepository;
    
    @Autowired
    private TestMethodRepository methodRepository;
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Autowired
    private TestCaseService testCaseService;
    
    @MockBean
    private RestTemplate restTemplate;
    
    private TestRequirement testRequirement;
    private TestLayer testLayer;
    private TestDesignMethod testMethod;
    private CaseGenerationTask task;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        testCaseRepository.deleteAll();
        taskRepository.deleteAll();
        requirementRepository.deleteAll();
        layerRepository.deleteAll();
        methodRepository.deleteAll();
        
        // 设置RestTemplate Mock
        if (intelligentCaseGenerationService instanceof IntelligentCaseGenerationServiceImpl) {
            IntelligentCaseGenerationServiceImpl serviceImpl = 
                (IntelligentCaseGenerationServiceImpl) intelligentCaseGenerationService;
            ReflectionTestUtils.setField(serviceImpl, "restTemplate", restTemplate);
            ReflectionTestUtils.setField(serviceImpl, "aiServiceUrl", "http://localhost:8000");
        }
        
        // 创建测试数据
        testRequirement = new TestRequirement();
        testRequirement.setRequirementCode("REQ-20240101-001");
        testRequirement.setRequirementName("测试需求");
        testRequirement.setRequirementDescription("这是一个测试需求描述，用于生成测试用例");
        testRequirement.setRequirementType("新功能");
        testRequirement.setRequirementStatus(RequirementStatus.DRAFT.name());
        testRequirement.setCreateTime(LocalDateTime.now());
        testRequirement = requirementRepository.save(testRequirement);
        
        testLayer = new TestLayer();
        testLayer.setLayerCode("UNIT");
        testLayer.setLayerName("单元测试");
        testLayer.setIsActive("1");
        testLayer.setLayerOrder(1);
        testLayer = layerRepository.save(testLayer);
        
        testMethod = new TestDesignMethod();
        testMethod.setMethodCode("EQUIVALENCE");
        testMethod.setMethodName("等价类划分");
        testMethod.setIsActive("1");
        testMethod = methodRepository.save(testMethod);
        
        // 创建任务
        task = new CaseGenerationTask();
        task.setTaskCode("TASK-20240101-001");
        task.setRequirementId(testRequirement.getId());
        task.setLayerId(testLayer.getId());
        task.setMethodId(testMethod.getId());
        task.setModelCode("DEEPSEEK");
        task.setTaskStatus("PENDING");
        task.setProgress(0);
        task.setCreateTime(LocalDateTime.now());
        task = taskRepository.save(task);
    }
    
    @Test
    @DisplayName("执行用例生成任务-成功")
    void testExecuteGenerationTask_Success() throws Exception {
        // Given - Mock Python服务响应
        Map<String, Object> pythonResponse = new HashMap<>();
        pythonResponse.put("status", "success");
        pythonResponse.put("message", "用例生成成功");
        
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> case1 = new HashMap<>();
        case1.put("case_name", "测试用例1");
        case1.put("test_steps", "1. 步骤一\n2. 步骤二");
        case1.put("expected_result", "预期结果1");
        case1.put("preconditions", "前置条件1");
        cases.add(case1);
        
        Map<String, Object> case2 = new HashMap<>();
        case2.put("case_name", "测试用例2");
        case2.put("test_steps", "1. 步骤一\n2. 步骤二");
        case2.put("expected_result", "预期结果2");
        cases.add(case2);
        
        pythonResponse.put("cases", cases);
        
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(pythonResponse);
        
        // When
        intelligentCaseGenerationService.executeGenerationTask(task.getId());
        
        // Then - 等待异步执行完成（轮询检查任务状态）
        waitForTaskCompletion(task.getId(), 5000); // 最多等待5秒
        
        // 验证任务状态
        CaseGenerationTask updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals("SUCCESS", updatedTask.getTaskStatus());
        assertEquals(100, updatedTask.getProgress());
        assertEquals(2, updatedTask.getTotalCases());
        assertEquals(2, updatedTask.getSuccessCases());
        assertEquals(0, updatedTask.getFailCases());
        assertNotNull(updatedTask.getCompleteTime());
        assertNotNull(updatedTask.getResultData());
        
        // 验证用例已保存
        List<TestCase> savedCases = testCaseRepository.findByRequirementId(testRequirement.getId());
        assertEquals(2, savedCases.size());
        assertTrue(savedCases.stream().anyMatch(c -> c.getCaseName().equals("测试用例1")));
        assertTrue(savedCases.stream().anyMatch(c -> c.getCaseName().equals("测试用例2")));
        
        // 验证RestTemplate被调用
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(Map.class));
    }
    
    @Test
    @DisplayName("执行用例生成任务-Python服务返回失败")
    void testExecuteGenerationTask_PythonServiceFailure() throws Exception {
        // Given - Mock Python服务返回失败
        Map<String, Object> pythonResponse = new HashMap<>();
        pythonResponse.put("status", "error");
        pythonResponse.put("message", "生成用例失败：模型调用异常");
        
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(pythonResponse);
        
        // When
        intelligentCaseGenerationService.executeGenerationTask(task.getId());
        
        // Then - 等待异步执行完成
        waitForTaskCompletion(task.getId(), 5000);
        
        // 验证任务状态为失败
        CaseGenerationTask updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals("FAILED", updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getErrorMessage());
        assertTrue(updatedTask.getErrorMessage().contains("用例生成失败"));
        assertNotNull(updatedTask.getCompleteTime());
        
        // 验证没有用例被保存
        List<TestCase> savedCases = testCaseRepository.findByRequirementId(testRequirement.getId());
        assertEquals(0, savedCases.size());
    }
    
    @Test
    @DisplayName("执行用例生成任务-Python服务返回空用例列表")
    void testExecuteGenerationTask_EmptyCases() throws Exception {
        // Given - Mock Python服务返回空用例列表
        Map<String, Object> pythonResponse = new HashMap<>();
        pythonResponse.put("status", "success");
        pythonResponse.put("cases", new ArrayList<>());
        
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(pythonResponse);
        
        // When
        intelligentCaseGenerationService.executeGenerationTask(task.getId());
        
        // Then - 等待异步执行完成
        waitForTaskCompletion(task.getId(), 5000);
        
        // 验证任务状态为失败
        CaseGenerationTask updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals("FAILED", updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getErrorMessage());
        assertTrue(updatedTask.getErrorMessage().contains("未生成任何用例"));
    }
    
    @Test
    @DisplayName("执行用例生成任务-任务不存在")
    void testExecuteGenerationTask_TaskNotFound() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            intelligentCaseGenerationService.executeGenerationTask(999L);
        });
        
        assertEquals("任务不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("执行用例生成任务-需求不存在")
    void testExecuteGenerationTask_RequirementNotFound() {
        // Given - 创建一个任务，但删除关联的需求
        CaseGenerationTask invalidTask = new CaseGenerationTask();
        invalidTask.setTaskCode("TASK-20240101-002");
        invalidTask.setRequirementId(999L); // 不存在的需求ID
        invalidTask.setLayerId(testLayer.getId());
        invalidTask.setMethodId(testMethod.getId());
        invalidTask.setModelCode("DEEPSEEK");
        invalidTask.setTaskStatus("PENDING");
        invalidTask.setProgress(0);
        invalidTask.setCreateTime(LocalDateTime.now());
        invalidTask = taskRepository.save(invalidTask);
        
        // When
        intelligentCaseGenerationService.executeGenerationTask(invalidTask.getId());
        
        // Then - 等待异步执行完成
        waitForTaskCompletion(invalidTask.getId(), 5000);
        
        // 验证任务状态为失败
        CaseGenerationTask updatedTask = taskRepository.findById(invalidTask.getId()).orElseThrow();
        assertEquals("FAILED", updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getErrorMessage());
        assertTrue(updatedTask.getErrorMessage().contains("需求不存在"));
    }
    
    @Test
    @DisplayName("执行用例生成任务-部分用例保存失败")
    void testExecuteGenerationTask_PartialCaseSaveFailure() throws Exception {
        // Given - Mock Python服务响应，包含一个无效的用例数据
        Map<String, Object> pythonResponse = new HashMap<>();
        pythonResponse.put("status", "success");
        
        List<Map<String, Object>> cases = new ArrayList<>();
        // 正常用例
        Map<String, Object> case1 = new HashMap<>();
        case1.put("case_name", "测试用例1");
        case1.put("test_steps", "1. 步骤一\n2. 步骤二");
        case1.put("expected_result", "预期结果1");
        cases.add(case1);
        
        // 无效用例（缺少必要字段，会导致保存失败）
        Map<String, Object> case2 = new HashMap<>();
        // 缺少case_name，会导致转换失败
        case2.put("test_steps", "1. 步骤一");
        cases.add(case2);
        
        pythonResponse.put("cases", cases);
        
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(pythonResponse);
        
        // When
        intelligentCaseGenerationService.executeGenerationTask(task.getId());
        
        // Then - 等待异步执行完成
        waitForTaskCompletion(task.getId(), 5000);
        
        // 验证任务状态（应该成功，但部分用例失败）
        CaseGenerationTask updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals("SUCCESS", updatedTask.getTaskStatus());
        assertEquals(2, updatedTask.getTotalCases());
        assertEquals(1, updatedTask.getSuccessCases()); // 只有1个成功
        assertEquals(1, updatedTask.getFailCases()); // 1个失败
    }
    
    @Test
    @DisplayName("执行用例生成任务-任务进度更新")
    void testExecuteGenerationTask_ProgressUpdate() throws Exception {
        // Given - Mock Python服务响应
        Map<String, Object> pythonResponse = new HashMap<>();
        pythonResponse.put("status", "success");
        
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> case1 = new HashMap<>();
        case1.put("case_name", "测试用例1");
        case1.put("test_steps", "1. 步骤一");
        case1.put("expected_result", "预期结果1");
        cases.add(case1);
        
        pythonResponse.put("cases", cases);
        
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(pythonResponse);
        
        // When
        intelligentCaseGenerationService.executeGenerationTask(task.getId());
        
        // Then - 等待异步执行完成
        waitForTaskCompletion(task.getId(), 5000);
        
        // 验证任务进度已更新（最终应该是100）
        CaseGenerationTask updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(100, updatedTask.getProgress());
    }
    
    /**
     * 等待任务完成（轮询检查任务状态）
     * @param taskId 任务ID
     * @param timeoutMs 超时时间（毫秒）
     */
    private void waitForTaskCompletion(Long taskId, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            CaseGenerationTask task = taskRepository.findById(taskId).orElse(null);
            if (task != null && 
                ("SUCCESS".equals(task.getTaskStatus()) || "FAILED".equals(task.getTaskStatus()))) {
                return; // 任务已完成
            }
            try {
                Thread.sleep(100); // 等待100ms后再次检查
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        // 超时后不抛出异常，让测试继续执行以验证实际状态
    }
}

