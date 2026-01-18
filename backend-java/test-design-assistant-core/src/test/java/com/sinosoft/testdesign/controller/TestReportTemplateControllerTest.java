package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.TestReportTemplateRequestDTO;
import com.sinosoft.testdesign.dto.TestReportTemplateResponseDTO;
import com.sinosoft.testdesign.entity.TestReportTemplate;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestReportTemplateService;
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
 * 测试报告模板管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("测试报告模板管理Controller测试")
class TestReportTemplateControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestReportTemplateService templateService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建报告模板-成功")
    void testCreateTemplate_Success() throws Exception {
        // Given
        TestReportTemplateRequestDTO dto = new TestReportTemplateRequestDTO();
        dto.setTemplateName("测试报告模板");
        dto.setTemplateType("EXECUTION");
        dto.setTemplateContent("{\"title\": \"测试报告\", \"sections\": []}");
        
        TestReportTemplate template = new TestReportTemplate();
        template.setId(1L);
        template.setTemplateCode("TMP-20240117-001");
        template.setTemplateName("测试报告模板");
        
        TestReportTemplateResponseDTO responseDTO = new TestReportTemplateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTemplateCode("TMP-20240117-001");
        responseDTO.setTemplateName("测试报告模板");
        
        when(entityDTOMapper.toTestReportTemplateEntity(any(TestReportTemplateRequestDTO.class)))
            .thenReturn(template);
        when(templateService.createTemplate(any(TestReportTemplate.class)))
            .thenReturn(template);
        when(entityDTOMapper.toTestReportTemplateResponseDTO(any(TestReportTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-report-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.templateCode").value("TMP-20240117-001"));
    }
    
    @Test
    @DisplayName("查询报告模板列表-成功")
    void testGetTemplateList_Success() throws Exception {
        // Given
        TestReportTemplate template = new TestReportTemplate();
        template.setId(1L);
        template.setTemplateCode("TMP-20240117-001");
        template.setTemplateName("测试报告模板");
        
        List<TestReportTemplate> templates = new ArrayList<>();
        templates.add(template);
        Page<TestReportTemplate> page = new PageImpl<>(templates, PageRequest.of(0, 10), 1);
        
        TestReportTemplateResponseDTO responseDTO = new TestReportTemplateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTemplateCode("TMP-20240117-001");
        
        when(templateService.getTemplateList(any()))
            .thenReturn(page);
        when(entityDTOMapper.toTestReportTemplateResponseDTO(any(TestReportTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-report-templates")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取报告模板详情-成功")
    void testGetTemplateById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestReportTemplate template = new TestReportTemplate();
        template.setId(id);
        template.setTemplateCode("TMP-20240117-001");
        
        TestReportTemplateResponseDTO responseDTO = new TestReportTemplateResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTemplateCode("TMP-20240117-001");
        
        when(templateService.getTemplateById(id))
            .thenReturn(template);
        when(entityDTOMapper.toTestReportTemplateResponseDTO(any(TestReportTemplate.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-report-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
    
    @Test
    @DisplayName("删除报告模板-成功")
    void testDeleteTemplate_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/test-report-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

