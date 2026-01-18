package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.PromptTemplateRequestDTO;
import com.sinosoft.testdesign.dto.PromptTemplateResponseDTO;
import com.sinosoft.testdesign.entity.PromptTemplate;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.PromptTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 提示词模板管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("提示词模板管理Controller测试")
class PromptTemplateControllerTest extends BaseControllerTest {
    
    @MockBean
    private PromptTemplateService templateService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建模板-成功")
    void testCreateTemplate_Success() throws Exception {
        // Given
        PromptTemplateRequestDTO dto = new PromptTemplateRequestDTO();
        dto.setTemplateName("新模板");
        dto.setTemplateContent("模板内容{var}");
        
        PromptTemplate template = new PromptTemplate();
        template.setTemplateName("新模板");
        template.setTemplateContent("模板内容{var}");
        
        PromptTemplate savedTemplate = new PromptTemplate();
        savedTemplate.setId(1L);
        savedTemplate.setTemplateCode("TMP-20240101-001");
        savedTemplate.setTemplateName("新模板");
        savedTemplate.setTemplateContent("模板内容{var}");
        savedTemplate.setIsActive("1");
        
        PromptTemplateResponseDTO responseDTO = new PromptTemplateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTemplateCode("TMP-20240101-001");
        responseDTO.setTemplateName("新模板");
        
        when(entityDTOMapper.toPromptTemplateEntity(any(PromptTemplateRequestDTO.class)))
            .thenReturn(template);
        when(templateService.createTemplate(any(PromptTemplate.class)))
            .thenReturn(savedTemplate);
        when(entityDTOMapper.toPromptTemplateResponseDTO(any(PromptTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/prompt-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.templateCode").value("TMP-20240101-001"))
                .andExpect(jsonPath("$.data.templateName").value("新模板"));
    }
    
    @Test
    @DisplayName("查询模板-根据ID")
    void testGetTemplateById_Success() throws Exception {
        // Given
        Long id = 1L;
        PromptTemplate template = new PromptTemplate();
        template.setId(id);
        template.setTemplateCode("TMP-20240101-001");
        template.setTemplateName("测试模板");
        
        PromptTemplateResponseDTO responseDTO = new PromptTemplateResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTemplateCode("TMP-20240101-001");
        responseDTO.setTemplateName("测试模板");
        
        when(templateService.getTemplateById(id))
            .thenReturn(template);
        when(entityDTOMapper.toPromptTemplateResponseDTO(any(PromptTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/prompt-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.templateCode").value("TMP-20240101-001"))
                .andExpect(jsonPath("$.data.templateName").value("测试模板"));
    }
    
    @Test
    @DisplayName("分页查询模板列表-成功")
    void testGetTemplateList_Success() throws Exception {
        // Given
        List<PromptTemplate> templates = new ArrayList<>();
        PromptTemplate template1 = new PromptTemplate();
        template1.setId(1L);
        template1.setTemplateCode("TMP-20240101-001");
        template1.setTemplateName("模板1");
        templates.add(template1);
        
        Page<PromptTemplate> page = new PageImpl<>(templates, PageRequest.of(0, 10), 1);
        
        when(templateService.getTemplateList(any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/v1/prompt-templates")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("更新模板-成功")
    void testUpdateTemplate_Success() throws Exception {
        // Given
        Long id = 1L;
        PromptTemplateRequestDTO dto = new PromptTemplateRequestDTO();
        dto.setTemplateName("更新后的模板名称");
        dto.setTemplateContent("更新后的模板内容");
        
        PromptTemplate existingTemplate = new PromptTemplate();
        existingTemplate.setId(id);
        existingTemplate.setTemplateCode("TMP-20240101-001");
        existingTemplate.setTemplateName("原模板名称");
        
        PromptTemplate updatedTemplate = new PromptTemplate();
        updatedTemplate.setId(id);
        updatedTemplate.setTemplateCode("TMP-20240101-001");
        updatedTemplate.setTemplateName("更新后的模板名称");
        
        PromptTemplateResponseDTO responseDTO = new PromptTemplateResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTemplateCode("TMP-20240101-001");
        responseDTO.setTemplateName("更新后的模板名称");
        
        when(templateService.getTemplateById(id))
            .thenReturn(existingTemplate);
        doNothing().when(entityDTOMapper).updatePromptTemplateFromDTO(any(PromptTemplateRequestDTO.class), any(PromptTemplate.class));
        when(templateService.updateTemplate(eq(id), any(PromptTemplate.class)))
            .thenReturn(updatedTemplate);
        when(entityDTOMapper.toPromptTemplateResponseDTO(any(PromptTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(put("/v1/prompt-templates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.templateName").value("更新后的模板名称"));
    }
    
    @Test
    @DisplayName("删除模板-成功")
    void testDeleteTemplate_Success() throws Exception {
        // Given
        Long id = 1L;
        
        doNothing().when(templateService).deleteTemplate(id);
        
        // When & Then
        mockMvc.perform(delete("/v1/prompt-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    @DisplayName("生成提示词-成功")
    void testGeneratePrompt_Success() throws Exception {
        // Given
        Long templateId = 1L;
        Map<String, Object> request = new HashMap<>();
        request.put("variables", Map.of("var1", "值1", "var2", "值2"));
        
        String generatedPrompt = "这是一个测试模板，包含变量值1和值2";
        
        when(templateService.generatePrompt(eq(templateId), any(Map.class)))
            .thenReturn(generatedPrompt);
        
        // When & Then
        mockMvc.perform(post("/v1/prompt-templates/{id}/generate", templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(generatedPrompt));
    }
}

