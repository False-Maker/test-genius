package com.sinosoft.testdesign.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.RequirementStatus;
import com.sinosoft.testdesign.service.RequirementAnalysisService;
import com.sinosoft.testdesign.service.RequirementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 需求管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@WebMvcTest(RequirementController.class)
@DisplayName("需求管理Controller测试")
class RequirementControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RequirementService requirementService;
    
    @MockBean
    private RequirementAnalysisService requirementAnalysisService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("创建需求-成功")
    void testCreateRequirement_Success() throws Exception {
        // Given
        TestRequirement requirement = new TestRequirement();
        requirement.setRequirementName("测试需求");
        requirement.setRequirementType("新功能");
        
        TestRequirement savedRequirement = new TestRequirement();
        savedRequirement.setId(1L);
        savedRequirement.setRequirementCode("REQ-20240101-001");
        savedRequirement.setRequirementName("测试需求");
        savedRequirement.setRequirementType("新功能");
        savedRequirement.setRequirementStatus(RequirementStatus.DRAFT.name());
        
        when(requirementService.createRequirement(any(TestRequirement.class)))
            .thenReturn(savedRequirement);
        
        // When & Then
        mockMvc.perform(post("/v1/requirements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requirement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.requirementCode").value("REQ-20240101-001"))
                .andExpect(jsonPath("$.data.requirementName").value("测试需求"));
    }
    
    @Test
    @DisplayName("查询需求-根据ID")
    void testGetRequirementById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestRequirement requirement = new TestRequirement();
        requirement.setId(id);
        requirement.setRequirementCode("REQ-20240101-001");
        requirement.setRequirementName("测试需求");
        
        when(requirementService.getRequirementById(id))
            .thenReturn(requirement);
        
        // When & Then
        mockMvc.perform(get("/v1/requirements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.requirementCode").value("REQ-20240101-001"))
                .andExpect(jsonPath("$.data.requirementName").value("测试需求"));
    }
    
    @Test
    @DisplayName("分页查询需求列表-成功")
    void testGetRequirementList_Success() throws Exception {
        // Given
        List<TestRequirement> requirements = new ArrayList<>();
        TestRequirement req1 = new TestRequirement();
        req1.setId(1L);
        req1.setRequirementCode("REQ-20240101-001");
        req1.setRequirementName("需求1");
        requirements.add(req1);
        
        Page<TestRequirement> page = new PageImpl<>(requirements, PageRequest.of(0, 10), 1);
        
        when(requirementService.getRequirementList(any(), any(), any()))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/v1/requirements")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("更新需求-成功")
    void testUpdateRequirement_Success() throws Exception {
        // Given
        Long id = 1L;
        TestRequirement requirement = new TestRequirement();
        requirement.setRequirementName("更新后的需求名称");
        
        TestRequirement updatedRequirement = new TestRequirement();
        updatedRequirement.setId(id);
        updatedRequirement.setRequirementCode("REQ-20240101-001");
        updatedRequirement.setRequirementName("更新后的需求名称");
        
        when(requirementService.updateRequirement(eq(id), any(TestRequirement.class)))
            .thenReturn(updatedRequirement);
        
        // When & Then
        mockMvc.perform(put("/v1/requirements/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requirement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requirementName").value("更新后的需求名称"));
    }
    
    @Test
    @DisplayName("删除需求-成功")
    void testDeleteRequirement_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/requirements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

