package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.service.AIServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UI脚本修复服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UI脚本修复服务测试")
class UIScriptRepairServiceImplTest {
    
    @Mock
    private AIServiceClient aiServiceClient;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private UIScriptRepairServiceImpl scriptRepairService;
    
    @BeforeEach
    void setUp() {
        // 设置AI服务URL
        ReflectionTestUtils.setField(scriptRepairService, "aiServiceUrl", "http://localhost:8000");
    }
    
    @Test
    @DisplayName("分析错误日志-成功")
    void testAnalyzeError_Success() {
        // Given
        String errorLog = "ElementNotFoundException: Unable to locate element with id: login-btn";
        String scriptContent = "driver.find_element(By.ID, 'login-btn').click()";
        
        Map<String, Object> response = new HashMap<>();
        response.put("error_type", "ELEMENT_NOT_FOUND");
        response.put("error_message", "无法定位元素");
        response.put("suggested_fix", "更新元素定位器");
        
        when(aiServiceClient.post(anyString(), any(Map.class)))
            .thenReturn(response);
        
        // When
        Map<String, Object> result = scriptRepairService.analyzeError(errorLog, scriptContent, false);
        
        // Then
        assertNotNull(result);
        assertEquals("ELEMENT_NOT_FOUND", result.get("error_type"));
        verify(aiServiceClient, times(1)).post(anyString(), any(Map.class));
    }
    
    @Test
    @DisplayName("分析错误日志-使用LLM")
    void testAnalyzeError_WithLlm() {
        // Given
        String errorLog = "TimeoutException: Element not found within 10 seconds";
        
        Map<String, Object> response = new HashMap<>();
        response.put("error_type", "TIMEOUT");
        response.put("error_message", "元素定位超时");
        
        when(aiServiceClient.post(anyString(), any(Map.class)))
            .thenReturn(response);
        
        // When
        Map<String, Object> result = scriptRepairService.analyzeError(errorLog, null, true);
        
        // Then
        assertNotNull(result);
        assertEquals("TIMEOUT", result.get("error_type"));
        verify(aiServiceClient, times(1)).post(anyString(), any(Map.class));
    }
    
    @Test
    @DisplayName("分析错误日志-服务返回空响应")
    void testAnalyzeError_EmptyResponse() {
        // Given
        String errorLog = "Some error";
        
        when(aiServiceClient.post(anyString(), any(Map.class)))
            .thenReturn(null);
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            scriptRepairService.analyzeError(errorLog, null, false);
        });
    }
    
    @Test
    @DisplayName("检测页面变化-成功")
    void testDetectPageChanges_Success() {
        // Given
        String oldPageCodeUrl = "http://example.com/old.html";
        String newPageCodeUrl = "http://example.com/new.html";
        
        List<Map<String, Object>> oldElements = new ArrayList<>();
        Map<String, Object> oldElement = new HashMap<>();
        oldElement.put("locator", "id=login-btn");
        oldElements.add(oldElement);
        
        List<Map<String, Object>> newElements = new ArrayList<>();
        Map<String, Object> newElement = new HashMap<>();
        newElement.put("locator", "class=login-button");
        newElements.add(newElement);
        
        Map<String, Object> response = new HashMap<>();
        response.put("has_changes", true);
        response.put("changed_elements", newElements);
        
        when(aiServiceClient.post(anyString(), any(Map.class)))
            .thenReturn(response);
        
        // When
        Map<String, Object> result = scriptRepairService.detectPageChanges(
            oldPageCodeUrl, oldElements, newPageCodeUrl, newElements, null);
        
        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("has_changes"));
        verify(aiServiceClient, times(1)).post(anyString(), any(Map.class));
    }
    
    @Test
    @DisplayName("修复UI脚本-成功")
    void testRepairScript_Success() {
        // Given
        String scriptContent = "driver.find_element(By.ID, 'login-btn').click()";
        String errorLog = "ElementNotFoundException: Unable to locate element";
        
        Map<String, Object> errorAnalysis = new HashMap<>();
        errorAnalysis.put("error_type", "ELEMENT_NOT_FOUND");
        
        Map<String, Object> response = new HashMap<>();
        response.put("repaired_script", "driver.find_element(By.CLASS_NAME, 'login-button').click()");
        response.put("repair_strategy", "UPDATE_LOCATOR");
        response.put("changes", "更新了元素定位器");
        
        when(aiServiceClient.post(anyString(), any(Map.class)))
            .thenReturn(response);
        
        // When
        Map<String, Object> result = scriptRepairService.repairScript(
            scriptContent, errorLog, errorAnalysis, null, null, null, "SELENIUM", "PYTHON", false);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.get("repaired_script"));
        verify(aiServiceClient, times(1)).post(anyString(), any(Map.class));
    }
    
    @Test
    @DisplayName("修复UI脚本-使用LLM")
    void testRepairScript_WithLlm() {
        // Given
        String scriptContent = "driver.find_element(By.ID, 'login-btn').click()";
        String errorLog = "TimeoutException";
        
        Map<String, Object> response = new HashMap<>();
        response.put("repaired_script", "from selenium.webdriver.support.ui import WebDriverWait\n" +
            "from selenium.webdriver.support import expected_conditions as EC\n" +
            "WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.ID, 'login-btn'))).click()");
        response.put("repair_strategy", "ADD_WAIT");
        
        when(aiServiceClient.post(anyString(), any(Map.class)))
            .thenReturn(response);
        
        // When
        Map<String, Object> result = scriptRepairService.repairScript(
            scriptContent, errorLog, null, null, null, null, "SELENIUM", "PYTHON", true);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.get("repaired_script"));
        verify(aiServiceClient, times(1)).post(anyString(), any(Map.class));
    }
    
    @Test
    @DisplayName("修复UI脚本-脚本内容为空")
    void testRepairScript_ScriptContentEmpty() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            scriptRepairService.repairScript("", "error", null, null, null, null, "SELENIUM", "PYTHON", false);
        });
        verify(aiServiceClient, never()).post(anyString(), any(Map.class));
    }
}

