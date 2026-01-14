package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.repository.ModelConfigRepository;
import com.sinosoft.testdesign.service.CacheService;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 模型配置管理服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("模型配置管理服务测试")
class ModelConfigServiceImplTest {
    
    @Mock
    private ModelConfigRepository modelConfigRepository;
    
    @Mock
    private CacheService cacheService;
    
    @InjectMocks
    private ModelConfigServiceImpl modelConfigService;
    
    private ModelConfig modelConfig;
    
    @BeforeEach
    void setUp() {
        modelConfig = new ModelConfig();
        modelConfig.setId(1L);
        modelConfig.setModelCode("DEEPSEEK-001");
        modelConfig.setModelName("DeepSeek Chat");
        modelConfig.setModelType("LLM");
        modelConfig.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        modelConfig.setApiKey("test-api-key");
        modelConfig.setIsActive("1");
        modelConfig.setPriority(1);
        modelConfig.setMaxTokens(4096);
        modelConfig.setTemperature(new BigDecimal("0.7"));
    }
    
    @Test
    @DisplayName("创建模型配置-成功")
    void testCreateModelConfig_Success() {
        // Given
        ModelConfig newConfig = new ModelConfig();
        newConfig.setModelCode("DEEPSEEK-002");
        newConfig.setModelName("DeepSeek Chat V2");
        newConfig.setModelType("LLM");
        newConfig.setApiEndpoint("https://api.deepseek.com/v2/chat/completions");
        
        when(modelConfigRepository.findByModelCode("DEEPSEEK-002"))
            .thenReturn(Optional.empty());
        when(modelConfigRepository.findAll())
            .thenReturn(new ArrayList<>());
        when(modelConfigRepository.save(any(ModelConfig.class)))
            .thenAnswer(invocation -> {
                ModelConfig config = invocation.getArgument(0);
                config.setId(2L);
                return config;
            });
        doNothing().when(cacheService).delete(anyString());
        doNothing().when(cacheService).deleteByPattern(anyString());
        
        // When
        ModelConfig result = modelConfigService.createModelConfig(newConfig);
        
        // Then
        assertNotNull(result);
        assertEquals("DEEPSEEK-002", result.getModelCode());
        assertEquals("1", result.getIsActive()); // 默认启用
        assertNotNull(result.getPriority()); // 自动设置优先级
        verify(modelConfigRepository, times(1)).save(any(ModelConfig.class));
        verify(cacheService, atLeastOnce()).delete(anyString());
    }
    
    @Test
    @DisplayName("创建模型配置-编码为空")
    void testCreateModelConfig_CodeEmpty() {
        // Given
        ModelConfig newConfig = new ModelConfig();
        newConfig.setModelName("测试模型");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.createModelConfig(newConfig);
        });
        
        assertEquals("模型编码不能为空", exception.getMessage());
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模型配置-名称为空")
    void testCreateModelConfig_NameEmpty() {
        // Given
        ModelConfig newConfig = new ModelConfig();
        newConfig.setModelCode("TEST-001");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.createModelConfig(newConfig);
        });
        
        assertEquals("模型名称不能为空", exception.getMessage());
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模型配置-编码已存在")
    void testCreateModelConfig_CodeExists() {
        // Given
        ModelConfig newConfig = new ModelConfig();
        newConfig.setModelCode("DEEPSEEK-001");
        newConfig.setModelName("测试模型");
        
        when(modelConfigRepository.findByModelCode("DEEPSEEK-001"))
            .thenReturn(Optional.of(modelConfig));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.createModelConfig(newConfig);
        });
        
        assertEquals("模型编码已存在: DEEPSEEK-001", exception.getMessage());
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模型配置-API端点格式错误")
    void testCreateModelConfig_InvalidEndpoint() {
        // Given
        ModelConfig newConfig = new ModelConfig();
        newConfig.setModelCode("TEST-001");
        newConfig.setModelName("测试模型");
        newConfig.setApiEndpoint("invalid-url"); // 无效的URL格式
        
        when(modelConfigRepository.findByModelCode("TEST-001"))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.createModelConfig(newConfig);
        });
        
        assertTrue(exception.getMessage().contains("API端点格式不正确"));
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模型配置-温度参数超出范围")
    void testCreateModelConfig_TemperatureOutOfRange() {
        // Given
        ModelConfig newConfig = new ModelConfig();
        newConfig.setModelCode("TEST-001");
        newConfig.setModelName("测试模型");
        newConfig.setApiEndpoint("https://api.test.com");
        newConfig.setTemperature(new BigDecimal("3.0")); // 超出0-2范围
        
        when(modelConfigRepository.findByModelCode("TEST-001"))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.createModelConfig(newConfig);
        });
        
        assertTrue(exception.getMessage().contains("温度参数必须在0-2之间"));
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新模型配置-成功")
    void testUpdateModelConfig_Success() {
        // Given
        Long id = 1L;
        ModelConfig updateConfig = new ModelConfig();
        updateConfig.setModelName("更新后的模型名称");
        updateConfig.setMaxTokens(8192);
        
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.of(modelConfig));
        when(modelConfigRepository.save(any(ModelConfig.class)))
            .thenReturn(modelConfig);
        doNothing().when(cacheService).delete(anyString());
        doNothing().when(cacheService).deleteByPattern(anyString());
        
        // When
        ModelConfig result = modelConfigService.updateModelConfig(id, updateConfig);
        
        // Then
        assertNotNull(result);
        verify(modelConfigRepository, times(1)).findById(id);
        verify(modelConfigRepository, times(1)).save(any(ModelConfig.class));
        verify(cacheService, atLeastOnce()).delete(anyString());
    }
    
    @Test
    @DisplayName("更新模型配置-不存在")
    void testUpdateModelConfig_NotFound() {
        // Given
        Long id = 999L;
        ModelConfig updateConfig = new ModelConfig();
        
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.updateModelConfig(id, updateConfig);
        });
        
        assertEquals("模型配置不存在", exception.getMessage());
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新模型配置-不允许修改编码")
    void testUpdateModelConfig_CannotModifyCode() {
        // Given
        Long id = 1L;
        ModelConfig updateConfig = new ModelConfig();
        updateConfig.setModelName("更新后的名称");
        updateConfig.setModelCode("NEW-CODE"); // 尝试修改编码
        
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.of(modelConfig));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.updateModelConfig(id, updateConfig);
        });
        
        assertEquals("不允许修改模型编码", exception.getMessage());
        verify(modelConfigRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询模型配置-根据ID（从缓存）")
    void testGetModelConfigById_FromCache() {
        // Given
        Long id = 1L;
        when(cacheService.get(anyString(), eq(ModelConfig.class)))
            .thenReturn(modelConfig);
        
        // When
        ModelConfig result = modelConfigService.getModelConfigById(id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(cacheService, times(1)).get(anyString(), eq(ModelConfig.class));
        verify(modelConfigRepository, never()).findById(any());
    }
    
    @Test
    @DisplayName("查询模型配置-根据ID（从数据库）")
    void testGetModelConfigById_FromDatabase() {
        // Given
        Long id = 1L;
        when(cacheService.get(anyString(), eq(ModelConfig.class)))
            .thenReturn(null);
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.of(modelConfig));
        doNothing().when(cacheService).set(anyString(), any(), anyInt());
        
        // When
        ModelConfig result = modelConfigService.getModelConfigById(id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(cacheService, times(1)).get(anyString(), eq(ModelConfig.class));
        verify(modelConfigRepository, times(1)).findById(id);
        verify(cacheService, times(1)).set(anyString(), any(), eq(3600));
    }
    
    @Test
    @DisplayName("查询模型配置-根据编码")
    void testGetModelConfigByCode_Success() {
        // Given
        String modelCode = "DEEPSEEK-001";
        when(cacheService.get(anyString(), eq(ModelConfig.class)))
            .thenReturn(null);
        when(modelConfigRepository.findByModelCode(modelCode))
            .thenReturn(Optional.of(modelConfig));
        doNothing().when(cacheService).set(anyString(), any(), anyInt());
        
        // When
        ModelConfig result = modelConfigService.getModelConfigByCode(modelCode);
        
        // Then
        assertNotNull(result);
        assertEquals(modelCode, result.getModelCode());
        verify(modelConfigRepository, times(1)).findByModelCode(modelCode);
    }
    
    @Test
    @DisplayName("分页查询模型配置列表-成功")
    void testGetModelConfigList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ModelConfig> configs = new ArrayList<>();
        configs.add(modelConfig);
        Page<ModelConfig> page = new PageImpl<>(configs, pageable, 1);
        
        when(modelConfigRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(page);
        
        // When
        Page<ModelConfig> result = modelConfigService.getModelConfigList(pageable, null, null, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(modelConfigRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("查询启用的模型配置列表-成功")
    void testGetActiveModelConfigs_Success() {
        // Given
        List<ModelConfig> configs = new ArrayList<>();
        configs.add(modelConfig);
        
        when(cacheService.getList(anyString(), eq(ModelConfig.class)))
            .thenReturn(null);
        when(modelConfigRepository.findByIsActiveOrderByPriorityAsc("1"))
            .thenReturn(configs);
        doNothing().when(cacheService).set(anyString(), any(), anyInt());
        
        // When
        List<ModelConfig> result = modelConfigService.getActiveModelConfigs();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(modelConfigRepository, times(1)).findByIsActiveOrderByPriorityAsc("1");
        verify(cacheService, times(1)).set(anyString(), any(), eq(1800));
    }
    
    @Test
    @DisplayName("删除模型配置-成功")
    void testDeleteModelConfig_Success() {
        // Given
        Long id = 1L;
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.of(modelConfig));
        doNothing().when(modelConfigRepository).delete(any(ModelConfig.class));
        doNothing().when(cacheService).delete(anyString());
        doNothing().when(cacheService).deleteByPattern(anyString());
        
        // When
        modelConfigService.deleteModelConfig(id);
        
        // Then
        verify(modelConfigRepository, times(1)).findById(id);
        verify(modelConfigRepository, times(1)).delete(any(ModelConfig.class));
        verify(cacheService, atLeastOnce()).delete(anyString());
    }
    
    @Test
    @DisplayName("切换模型配置状态-成功")
    void testToggleModelConfigStatus_Success() {
        // Given
        Long id = 1L;
        String newStatus = "0";
        
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.of(modelConfig));
        when(modelConfigRepository.save(any(ModelConfig.class)))
            .thenReturn(modelConfig);
        doNothing().when(cacheService).delete(anyString());
        doNothing().when(cacheService).deleteByPattern(anyString());
        
        // When
        ModelConfig result = modelConfigService.toggleModelConfigStatus(id, newStatus);
        
        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getIsActive());
        verify(modelConfigRepository, times(1)).findById(id);
        verify(modelConfigRepository, times(1)).save(any(ModelConfig.class));
    }
    
    @Test
    @DisplayName("切换模型配置状态-状态值无效")
    void testToggleModelConfigStatus_InvalidStatus() {
        // Given
        Long id = 1L;
        String invalidStatus = "2";
        
        when(modelConfigRepository.findById(id))
            .thenReturn(Optional.of(modelConfig));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            modelConfigService.toggleModelConfigStatus(id, invalidStatus);
        });
        
        assertEquals("状态值无效，必须为0或1", exception.getMessage());
        verify(modelConfigRepository, never()).save(any());
    }
}

