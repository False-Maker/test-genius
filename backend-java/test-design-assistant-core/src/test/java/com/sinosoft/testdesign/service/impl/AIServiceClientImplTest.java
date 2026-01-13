package com.sinosoft.testdesign.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AI服务客户端单元测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI服务客户端测试")
class AIServiceClientImplTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private AIServiceClientImpl aiServiceClient;
    
    private Map<String, Object> mockResponse;
    
    @BeforeEach
    void setUp() {
        mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("data", "test data");
    }
    
    @Test
    @DisplayName("POST请求-成功")
    void testPost_Success() {
        // Given
        String url = "http://localhost:8000/api/case/generate";
        Map<String, Object> request = new HashMap<>();
        request.put("requirement_id", 1L);
        request.put("requirement_text", "测试需求");
        
        when(restTemplate.postForObject(eq(url), eq(request), eq(Map.class)))
            .thenReturn(mockResponse);
        
        // When
        Map<String, Object> result = aiServiceClient.post(url, request);
        
        // Then
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("test data", result.get("data"));
        verify(restTemplate, times(1)).postForObject(eq(url), eq(request), eq(Map.class));
    }
    
    @Test
    @DisplayName("POST请求-返回null")
    void testPost_ReturnsNull() {
        // Given
        String url = "http://localhost:8000/api/case/generate";
        Map<String, Object> request = new HashMap<>();
        
        when(restTemplate.postForObject(eq(url), eq(request), eq(Map.class)))
            .thenReturn(null);
        
        // When
        Map<String, Object> result = aiServiceClient.post(url, request);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("POST请求-异常处理")
    void testPost_Exception() {
        // Given
        String url = "http://localhost:8000/api/case/generate";
        Map<String, Object> request = new HashMap<>();
        
        when(restTemplate.postForObject(eq(url), eq(request), eq(Map.class)))
            .thenThrow(new RestClientException("连接超时"));
        
        // When & Then
        assertThrows(RestClientException.class, () -> {
            aiServiceClient.post(url, request);
        });
    }
    
    @Test
    @DisplayName("GET请求-成功")
    void testGet_Success() {
        // Given
        String url = "http://localhost:8000/api/case/status/1";
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
            .thenReturn(mockResponse);
        
        // When
        Map<String, Object> result = aiServiceClient.get(url);
        
        // Then
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("test data", result.get("data"));
        verify(restTemplate, times(1)).getForObject(eq(url), eq(Map.class));
    }
    
    @Test
    @DisplayName("GET请求-返回null")
    void testGet_ReturnsNull() {
        // Given
        String url = "http://localhost:8000/api/case/status/1";
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
            .thenReturn(null);
        
        // When
        Map<String, Object> result = aiServiceClient.get(url);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("GET请求-异常处理")
    void testGet_Exception() {
        // Given
        String url = "http://localhost:8000/api/case/status/1";
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
            .thenThrow(new RestClientException("连接超时"));
        
        // When & Then
        assertThrows(RestClientException.class, () -> {
            aiServiceClient.get(url);
        });
    }
    
    @Test
    @DisplayName("POST请求-复杂请求体")
    void testPost_ComplexRequest() {
        // Given
        String url = "http://localhost:8000/api/case/generate";
        Map<String, Object> request = new HashMap<>();
        request.put("requirement_id", 1L);
        request.put("requirement_text", "测试需求");
        request.put("layer_code", "UNIT");
        request.put("method_code", "EQUIVALENCE");
        request.put("model_code", "DEEPSEEK");
        
        Map<String, Object> complexResponse = new HashMap<>();
        complexResponse.put("status", "success");
        complexResponse.put("cases", new Object[]{});
        
        when(restTemplate.postForObject(eq(url), eq(request), eq(Map.class)))
            .thenReturn(complexResponse);
        
        // When
        Map<String, Object> result = aiServiceClient.post(url, request);
        
        // Then
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertTrue(result.containsKey("cases"));
    }
    
    @Test
    @DisplayName("GET请求-带查询参数")
    void testGet_WithQueryParams() {
        // Given
        String url = "http://localhost:8000/api/case/list?page=1&size=10";
        
        Map<String, Object> listResponse = new HashMap<>();
        listResponse.put("status", "success");
        listResponse.put("total", 100);
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
            .thenReturn(listResponse);
        
        // When
        Map<String, Object> result = aiServiceClient.get(url);
        
        // Then
        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals(100, result.get("total"));
    }
}

