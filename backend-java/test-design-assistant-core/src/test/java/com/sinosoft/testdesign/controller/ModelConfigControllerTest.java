package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.ModelConfigRequestDTO;
import com.sinosoft.testdesign.dto.ModelConfigResponseDTO;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.ModelConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 模型配置管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("模型配置管理Controller测试")
class ModelConfigControllerTest extends BaseControllerTest {
    
    @MockBean
    private ModelConfigService modelConfigService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建模型配置-成功")
    void testCreateModelConfig_Success() throws Exception {
        // Given
        ModelConfigRequestDTO dto = new ModelConfigRequestDTO();
        dto.setModelName("DeepSeek Chat");
        dto.setModelType("LLM");
        dto.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setModelCode("DEEPSEEK-001");
        modelConfig.setModelName("DeepSeek Chat");
        modelConfig.setModelType("LLM");
        modelConfig.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        
        ModelConfig savedConfig = new ModelConfig();
        savedConfig.setId(1L);
        savedConfig.setModelCode("DEEPSEEK-001");
        savedConfig.setModelName("DeepSeek Chat");
        savedConfig.setModelType("LLM");
        savedConfig.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        savedConfig.setIsActive("1");
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setModelCode("DEEPSEEK-001");
        responseDTO.setModelName("DeepSeek Chat");
        responseDTO.setModelType("LLM");
        responseDTO.setApiEndpoint("https://api.deepseek.com/v1/chat/completions");
        
        when(entityDTOMapper.toModelConfigEntity(any(ModelConfigRequestDTO.class)))
            .thenReturn(modelConfig);
        when(modelConfigService.createModelConfig(any(ModelConfig.class)))
            .thenReturn(savedConfig);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/model-configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.modelCode").value("DEEPSEEK-001"))
                .andExpect(jsonPath("$.data.modelName").value("DeepSeek Chat"));
    }
    
    @Test
    @DisplayName("查询模型配置-根据ID")
    void testGetModelConfigById_Success() throws Exception {
        // Given
        Long id = 1L;
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setId(id);
        modelConfig.setModelCode("DEEPSEEK-001");
        modelConfig.setModelName("DeepSeek Chat");
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(id);
        responseDTO.setModelCode("DEEPSEEK-001");
        responseDTO.setModelName("DeepSeek Chat");
        
        when(modelConfigService.getModelConfigById(id))
            .thenReturn(modelConfig);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/model-configs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.modelCode").value("DEEPSEEK-001"))
                .andExpect(jsonPath("$.data.modelName").value("DeepSeek Chat"));
    }
    
    @Test
    @DisplayName("查询模型配置-根据编码")
    void testGetModelConfigByCode_Success() throws Exception {
        // Given
        String modelCode = "DEEPSEEK-001";
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setId(1L);
        modelConfig.setModelCode(modelCode);
        modelConfig.setModelName("DeepSeek Chat");
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setModelCode(modelCode);
        responseDTO.setModelName("DeepSeek Chat");
        
        when(modelConfigService.getModelConfigByCode(modelCode))
            .thenReturn(modelConfig);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/model-configs/code/{modelCode}", modelCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modelCode").value(modelCode))
                .andExpect(jsonPath("$.data.modelName").value("DeepSeek Chat"));
    }
    
    @Test
    @DisplayName("分页查询模型配置列表-成功")
    void testGetModelConfigList_Success() throws Exception {
        // Given
        List<ModelConfig> configs = new ArrayList<>();
        ModelConfig config1 = new ModelConfig();
        config1.setId(1L);
        config1.setModelCode("DEEPSEEK-001");
        config1.setModelName("DeepSeek Chat");
        configs.add(config1);
        
        Page<ModelConfig> page = new PageImpl<>(configs, PageRequest.of(0, 10), 1);
        
        when(modelConfigService.getModelConfigList(any(), any(), any(), any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/v1/model-configs")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("分页查询模型配置列表-按类型搜索")
    void testGetModelConfigList_ByType() throws Exception {
        // Given
        List<ModelConfig> configs = new ArrayList<>();
        ModelConfig config1 = new ModelConfig();
        config1.setId(1L);
        config1.setModelCode("DEEPSEEK-001");
        config1.setModelName("DeepSeek Chat");
        config1.setModelType("LLM");
        configs.add(config1);
        
        Page<ModelConfig> page = new PageImpl<>(configs, PageRequest.of(0, 10), 1);
        
        when(modelConfigService.getModelConfigList(any(), any(), eq("LLM"), any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/v1/model-configs")
                .param("page", "0")
                .param("size", "10")
                .param("modelType", "LLM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("更新模型配置-成功")
    void testUpdateModelConfig_Success() throws Exception {
        // Given
        Long id = 1L;
        ModelConfigRequestDTO dto = new ModelConfigRequestDTO();
        dto.setModelName("更新后的模型名称");
        dto.setMaxTokens(8192);
        
        ModelConfig existingConfig = new ModelConfig();
        existingConfig.setId(id);
        existingConfig.setModelCode("DEEPSEEK-001");
        existingConfig.setModelName("原模型名称");
        
        ModelConfig updatedConfig = new ModelConfig();
        updatedConfig.setId(id);
        updatedConfig.setModelCode("DEEPSEEK-001");
        updatedConfig.setModelName("更新后的模型名称");
        updatedConfig.setMaxTokens(8192);
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(id);
        responseDTO.setModelCode("DEEPSEEK-001");
        responseDTO.setModelName("更新后的模型名称");
        responseDTO.setMaxTokens(8192);
        
        when(modelConfigService.getModelConfigById(id))
            .thenReturn(existingConfig);
        doNothing().when(entityDTOMapper).updateModelConfigFromDTO(any(ModelConfigRequestDTO.class), any(ModelConfig.class));
        when(modelConfigService.updateModelConfig(eq(id), any(ModelConfig.class)))
            .thenReturn(updatedConfig);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(put("/v1/model-configs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modelName").value("更新后的模型名称"));
    }
    
    @Test
    @DisplayName("删除模型配置-成功")
    void testDeleteModelConfig_Success() throws Exception {
        // Given
        Long id = 1L;
        
        doNothing().when(modelConfigService).deleteModelConfig(id);
        
        // When & Then
        mockMvc.perform(delete("/v1/model-configs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    @DisplayName("切换模型配置状态-成功")
    void testToggleModelConfigStatus_Success() throws Exception {
        // Given
        Long id = 1L;
        String newStatus = "0";
        
        ModelConfig updatedConfig = new ModelConfig();
        updatedConfig.setId(id);
        updatedConfig.setModelCode("DEEPSEEK-001");
        updatedConfig.setIsActive(newStatus);
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(id);
        responseDTO.setModelCode("DEEPSEEK-001");
        responseDTO.setIsActive(newStatus);
        
        when(modelConfigService.toggleModelConfigStatus(id, newStatus))
            .thenReturn(updatedConfig);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(put("/v1/model-configs/{id}/status", id)
                .param("isActive", newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isActive").value(newStatus));
    }
    
    @Test
    @DisplayName("获取启用的模型配置列表-成功")
    void testGetActiveModelConfigs_Success() throws Exception {
        // Given
        List<ModelConfig> configs = new ArrayList<>();
        ModelConfig config1 = new ModelConfig();
        config1.setId(1L);
        config1.setModelCode("DEEPSEEK-001");
        config1.setModelName("DeepSeek Chat");
        config1.setIsActive("1");
        configs.add(config1);
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setModelCode("DEEPSEEK-001");
        responseDTO.setModelName("DeepSeek Chat");
        responseDTO.setIsActive("1");
        
        when(modelConfigService.getActiveModelConfigs())
            .thenReturn(configs);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/model-configs/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].isActive").value("1"));
    }
    
    @Test
    @DisplayName("根据类型获取启用的模型配置-成功")
    void testGetActiveModelConfigsByType_Success() throws Exception {
        // Given
        String modelType = "LLM";
        List<ModelConfig> configs = new ArrayList<>();
        ModelConfig config1 = new ModelConfig();
        config1.setId(1L);
        config1.setModelCode("DEEPSEEK-001");
        config1.setModelName("DeepSeek Chat");
        config1.setModelType(modelType);
        config1.setIsActive("1");
        configs.add(config1);
        
        ModelConfigResponseDTO responseDTO = new ModelConfigResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setModelCode("DEEPSEEK-001");
        responseDTO.setModelName("DeepSeek Chat");
        responseDTO.setModelType(modelType);
        responseDTO.setIsActive("1");
        
        when(modelConfigService.getActiveModelConfigsByType(modelType))
            .thenReturn(configs);
        when(entityDTOMapper.toModelConfigResponseDTO(any(ModelConfig.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/model-configs/active/type/{modelType}", modelType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].modelType").value(modelType));
    }
}

