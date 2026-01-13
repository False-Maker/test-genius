package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.entity.TestDesignMethod;
import com.sinosoft.testdesign.entity.TestLayer;
import com.sinosoft.testdesign.repository.ModelConfigRepository;
import com.sinosoft.testdesign.repository.TestLayerRepository;
import com.sinosoft.testdesign.repository.TestMethodRepository;
import com.sinosoft.testdesign.service.CacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 通用数据Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("通用数据Controller测试")
class CommonControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestLayerRepository testLayerRepository;
    
    @MockBean
    private TestMethodRepository testMethodRepository;
    
    @MockBean
    private ModelConfigRepository modelConfigRepository;
    
    @MockBean
    private CacheService cacheService;
    
    @Test
    @DisplayName("获取测试分层列表-从缓存")
    void testGetTestLayerList_FromCache() throws Exception {
        // Given
        List<TestLayer> cachedLayers = new ArrayList<>();
        TestLayer layer1 = new TestLayer();
        layer1.setId(1L);
        layer1.setLayerName("单元测试");
        layer1.setIsActive("1");
        cachedLayers.add(layer1);
        
        when(cacheService.getList(eq("cache:common:test-layers"), eq(TestLayer.class)))
            .thenReturn(cachedLayers);
        
        // When & Then
        mockMvc.perform(get("/v1/common/test-layers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].layerName").value("单元测试"));
        
        // 验证没有查询数据库
        verify(testLayerRepository, never()).findByIsActiveOrderByLayerOrder(anyString());
    }
    
    @Test
    @DisplayName("获取测试分层列表-从数据库")
    void testGetTestLayerList_FromDatabase() throws Exception {
        // Given
        List<TestLayer> layers = new ArrayList<>();
        TestLayer layer1 = new TestLayer();
        layer1.setId(1L);
        layer1.setLayerName("单元测试");
        layer1.setIsActive("1");
        layers.add(layer1);
        
        when(cacheService.getList(eq("cache:common:test-layers"), eq(TestLayer.class)))
            .thenReturn(null);
        when(testLayerRepository.findByIsActiveOrderByLayerOrder("1"))
            .thenReturn(layers);
        doNothing().when(cacheService).set(anyString(), any(), anyInt());
        
        // When & Then
        mockMvc.perform(get("/v1/common/test-layers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].layerName").value("单元测试"));
        
        // 验证查询了数据库并存入缓存
        verify(testLayerRepository, times(1)).findByIsActiveOrderByLayerOrder("1");
        verify(cacheService, times(1)).set(anyString(), any(), eq(3600));
    }
    
    @Test
    @DisplayName("获取测试设计方法列表-从缓存")
    void testGetTestDesignMethodList_FromCache() throws Exception {
        // Given
        List<TestDesignMethod> cachedMethods = new ArrayList<>();
        TestDesignMethod method1 = new TestDesignMethod();
        method1.setId(1L);
        method1.setMethodName("等价类划分");
        method1.setIsActive("1");
        cachedMethods.add(method1);
        
        when(cacheService.getList(eq("cache:common:test-methods"), eq(TestDesignMethod.class)))
            .thenReturn(cachedMethods);
        
        // When & Then
        mockMvc.perform(get("/v1/common/test-design-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].methodName").value("等价类划分"));
        
        // 验证没有查询数据库
        verify(testMethodRepository, never()).findByIsActive(anyString());
    }
    
    @Test
    @DisplayName("获取测试设计方法列表-从数据库")
    void testGetTestDesignMethodList_FromDatabase() throws Exception {
        // Given
        List<TestDesignMethod> methods = new ArrayList<>();
        TestDesignMethod method1 = new TestDesignMethod();
        method1.setId(1L);
        method1.setMethodName("等价类划分");
        method1.setIsActive("1");
        methods.add(method1);
        
        when(cacheService.getList(eq("cache:common:test-methods"), eq(TestDesignMethod.class)))
            .thenReturn(null);
        when(testMethodRepository.findByIsActive("1"))
            .thenReturn(methods);
        doNothing().when(cacheService).set(anyString(), any(), anyInt());
        
        // When & Then
        mockMvc.perform(get("/v1/common/test-design-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].methodName").value("等价类划分"));
        
        // 验证查询了数据库并存入缓存
        verify(testMethodRepository, times(1)).findByIsActive("1");
        verify(cacheService, times(1)).set(anyString(), any(), eq(3600));
    }
    
    @Test
    @DisplayName("获取模型配置列表-从缓存")
    void testGetModelConfigList_FromCache() throws Exception {
        // Given
        List<ModelConfig> cachedConfigs = new ArrayList<>();
        ModelConfig config1 = new ModelConfig();
        config1.setId(1L);
        config1.setModelCode("DEEPSEEK-001");
        config1.setModelName("DeepSeek Chat");
        config1.setIsActive("1");
        cachedConfigs.add(config1);
        
        when(cacheService.getList(eq("cache:common:model-configs"), eq(ModelConfig.class)))
            .thenReturn(cachedConfigs);
        
        // When & Then
        mockMvc.perform(get("/v1/common/model-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].modelCode").value("DEEPSEEK-001"));
        
        // 验证没有查询数据库
        verify(modelConfigRepository, never()).findByIsActiveOrderByPriorityAsc(anyString());
    }
    
    @Test
    @DisplayName("获取模型配置列表-从数据库")
    void testGetModelConfigList_FromDatabase() throws Exception {
        // Given
        List<ModelConfig> configs = new ArrayList<>();
        ModelConfig config1 = new ModelConfig();
        config1.setId(1L);
        config1.setModelCode("DEEPSEEK-001");
        config1.setModelName("DeepSeek Chat");
        config1.setIsActive("1");
        configs.add(config1);
        
        when(cacheService.getList(eq("cache:common:model-configs"), eq(ModelConfig.class)))
            .thenReturn(null);
        when(modelConfigRepository.findByIsActiveOrderByPriorityAsc("1"))
            .thenReturn(configs);
        doNothing().when(cacheService).set(anyString(), any(), anyInt());
        
        // When & Then
        mockMvc.perform(get("/v1/common/model-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].modelCode").value("DEEPSEEK-001"));
        
        // 验证查询了数据库并存入缓存
        verify(modelConfigRepository, times(1)).findByIsActiveOrderByPriorityAsc("1");
        verify(cacheService, times(1)).set(anyString(), any(), eq(1800));
    }
}

