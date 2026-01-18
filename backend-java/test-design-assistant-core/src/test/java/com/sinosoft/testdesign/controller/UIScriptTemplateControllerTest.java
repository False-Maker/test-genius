package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.UIScriptTemplateRequestDTO;
import com.sinosoft.testdesign.dto.UIScriptTemplateResponseDTO;
import com.sinosoft.testdesign.entity.UIScriptTemplate;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.UIScriptTemplateService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UI脚本模板Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("UI脚本模板Controller测试")
class UIScriptTemplateControllerTest extends BaseControllerTest {
    
    @MockBean
    private UIScriptTemplateService templateService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建脚本模板-成功")
    void testCreateTemplate_Success() throws Exception {
        // Given
        UIScriptTemplateRequestDTO dto = new UIScriptTemplateRequestDTO();
        dto.setTemplateName("Selenium模板");
        dto.setTemplateType("SELENIUM");
        dto.setScriptLanguage("PYTHON");
        dto.setTemplateContent("from selenium import webdriver\ndriver = webdriver.Chrome()");
        
        UIScriptTemplate template = new UIScriptTemplate();
        template.setId(1L);
        template.setTemplateCode("TMP-20240117-001");
        template.setTemplateName("Selenium模板");
        
        UIScriptTemplateResponseDTO responseDTO = new UIScriptTemplateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTemplateCode("TMP-20240117-001");
        responseDTO.setTemplateName("Selenium模板");
        
        when(entityDTOMapper.toUIScriptTemplateEntity(any(UIScriptTemplateRequestDTO.class)))
            .thenReturn(template);
        when(templateService.createTemplate(any(UIScriptTemplate.class)))
            .thenReturn(template);
        when(entityDTOMapper.toUIScriptTemplateResponseDTO(any(UIScriptTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/ui-script-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.templateCode").value("TMP-20240117-001"));
    }
    
    @Test
    @DisplayName("查询脚本模板列表-成功")
    void testGetTemplateList_Success() throws Exception {
        // Given
        UIScriptTemplate template = new UIScriptTemplate();
        template.setId(1L);
        template.setTemplateCode("TMP-20240117-001");
        template.setTemplateName("Selenium模板");
        
        List<UIScriptTemplate> templates = new ArrayList<>();
        templates.add(template);
        Page<UIScriptTemplate> page = new PageImpl<>(templates, PageRequest.of(0, 10), 1);
        
        UIScriptTemplateResponseDTO responseDTO = new UIScriptTemplateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTemplateCode("TMP-20240117-001");
        
        when(templateService.getTemplateList(any(), any(), any(), any(), any()))
            .thenReturn(page);
        when(entityDTOMapper.toUIScriptTemplateResponseDTO(any(UIScriptTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/ui-script-templates")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取脚本模板详情-成功")
    void testGetTemplateById_Success() throws Exception {
        // Given
        Long id = 1L;
        UIScriptTemplate template = new UIScriptTemplate();
        template.setId(id);
        template.setTemplateCode("TMP-20240117-001");
        
        UIScriptTemplateResponseDTO responseDTO = new UIScriptTemplateResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTemplateCode("TMP-20240117-001");
        
        when(templateService.getTemplateById(id))
            .thenReturn(template);
        when(entityDTOMapper.toUIScriptTemplateResponseDTO(any(UIScriptTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/ui-script-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
    
    @Test
    @DisplayName("删除脚本模板-成功")
    void testDeleteTemplate_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/ui-script-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

