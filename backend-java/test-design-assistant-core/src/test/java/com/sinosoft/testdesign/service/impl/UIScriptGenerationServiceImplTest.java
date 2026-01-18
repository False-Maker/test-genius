package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.UIScriptGenerationRequest;
import com.sinosoft.testdesign.dto.UIScriptGenerationResult;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.repository.PageElementInfoRepository;
import com.sinosoft.testdesign.repository.TestExecutionTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UI脚本生成服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UI脚本生成服务测试")
class UIScriptGenerationServiceImplTest {
    
    @Mock
    private TestExecutionTaskRepository taskRepository;
    
    @Mock
    private PageElementInfoRepository pageElementInfoRepository;
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private UIScriptGenerationServiceImpl scriptGenerationService;
    
    private TestExecutionTask testTask;
    private UIScriptGenerationRequest testRequest;
    
    @BeforeEach
    void setUp() {
        // 设置AI服务URL
        ReflectionTestUtils.setField(scriptGenerationService, "aiServiceUrl", "http://localhost:8000");
        
        testTask = new TestExecutionTask();
        testTask.setId(1L);
        testTask.setTaskCode("TASK-20240117-001");
        testTask.setTaskName("UI脚本生成任务");
        testTask.setTaskType("AUTO_SCRIPT_GENERATION");
        testTask.setTaskStatus("PENDING");
        testTask.setScriptType("SELENIUM");
        testTask.setScriptLanguage("PYTHON");
        
        testRequest = new UIScriptGenerationRequest();
        testRequest.setNaturalLanguageDesc("点击登录按钮");
        testRequest.setScriptType("SELENIUM");
        testRequest.setScriptLanguage("PYTHON");
        testRequest.setPageCodeUrl("http://example.com/page.html");
    }
    
    @Test
    @DisplayName("生成UI脚本-成功")
    void testGenerateScript_Success() {
        // Given
        when(taskRepository.findByTaskCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(taskRepository.save(any(TestExecutionTask.class)))
            .thenAnswer(invocation -> {
                TestExecutionTask task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        
        // When
        String result = scriptGenerationService.generateScript(testRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("TASK-"));
        verify(taskRepository, times(1)).save(any(TestExecutionTask.class));
    }
    
    @Test
    @DisplayName("生成UI脚本-自然语言描述为空")
    void testGenerateScript_NaturalLanguageDescEmpty() {
        // Given
        testRequest.setNaturalLanguageDesc("");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            scriptGenerationService.generateScript(testRequest);
        });
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询生成任务状态-成功")
    void testGetTaskStatus_Success() {
        // Given
        testTask.setTaskStatus("SUCCESS");
        testTask.setProgress(100);
        testTask.setScriptContent("from selenium import webdriver\ndriver = webdriver.Chrome()");
        
        when(taskRepository.findByTaskCode("TASK-20240117-001"))
            .thenReturn(Optional.of(testTask));
        
        // When
        UIScriptGenerationResult result = scriptGenerationService.getTaskStatus("TASK-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("SUCCESS", result.getTaskStatus());
        assertEquals(100, result.getProgress());
        assertNotNull(result.getScriptContent());
    }
    
    @Test
    @DisplayName("查询生成任务状态-任务不存在")
    void testGetTaskStatus_NotFound() {
        // Given
        when(taskRepository.findByTaskCode("TASK-20240117-999"))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            scriptGenerationService.getTaskStatus("TASK-20240117-999");
        });
    }
    
    @Test
    @DisplayName("解析页面代码-成功")
    void testParsePageCode_Success() throws Exception {
        // Given
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> element = new HashMap<>();
        element.put("elementType", "BUTTON");
        element.put("elementLocatorType", "ID");
        element.put("elementLocatorValue", "login-btn");
        elements.add(element);
        response.put("elements", elements);
        
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(response);
        
        // When
        List<PageElementInfo> result = scriptGenerationService.parsePageCode("http://example.com/page.html");
        
        // Then
        assertNotNull(result);
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(Map.class));
    }
    
    @Test
    @DisplayName("解析页面代码-URL为空")
    void testParsePageCode_UrlEmpty() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            scriptGenerationService.parsePageCode("");
        });
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }
}

