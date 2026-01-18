package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.FieldTestPoint;
import com.sinosoft.testdesign.entity.LogicTestPoint;
import com.sinosoft.testdesign.entity.TestSpecification;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.FieldTestPointService;
import com.sinosoft.testdesign.service.LogicTestPointService;
import com.sinosoft.testdesign.service.TestSpecificationService;
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
 * 测试规约管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("测试规约管理Controller测试")
class TestSpecificationControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestSpecificationService specificationService;
    
    @MockBean
    private FieldTestPointService fieldTestPointService;
    
    @MockBean
    private LogicTestPointService logicTestPointService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建测试规约-成功")
    void testCreateSpecification_Success() throws Exception {
        // Given
        TestSpecificationRequestDTO dto = new TestSpecificationRequestDTO();
        dto.setSpecName("测试规约");
        dto.setSpecType("APPLICATION");
        
        TestSpecification spec = new TestSpecification();
        spec.setId(1L);
        spec.setSpecCode("SPEC-20240117-001");
        spec.setSpecName("测试规约");
        
        TestSpecificationResponseDTO responseDTO = new TestSpecificationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setSpecCode("SPEC-20240117-001");
        responseDTO.setSpecName("测试规约");
        
        when(entityDTOMapper.toTestSpecificationEntity(any(TestSpecificationRequestDTO.class)))
            .thenReturn(spec);
        when(specificationService.createSpecification(any(TestSpecification.class)))
            .thenReturn(spec);
        when(entityDTOMapper.toTestSpecificationResponseDTO(any(TestSpecification.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/specifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.specCode").value("SPEC-20240117-001"));
    }
    
    @Test
    @DisplayName("查询测试规约列表-成功")
    void testGetSpecificationList_Success() throws Exception {
        // Given
        TestSpecification spec = new TestSpecification();
        spec.setId(1L);
        spec.setSpecCode("SPEC-20240117-001");
        spec.setSpecName("测试规约");
        
        List<TestSpecification> specs = new ArrayList<>();
        specs.add(spec);
        Page<TestSpecification> page = new PageImpl<>(specs, PageRequest.of(0, 10), 1);
        
        TestSpecificationResponseDTO responseDTO = new TestSpecificationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setSpecCode("SPEC-20240117-001");
        
        when(specificationService.getSpecificationList(any(), any(), any(), any()))
            .thenReturn(page);
        when(entityDTOMapper.toTestSpecificationResponseDTO(any(TestSpecification.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/specifications")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取测试规约详情-成功")
    void testGetSpecificationById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestSpecification spec = new TestSpecification();
        spec.setId(id);
        spec.setSpecCode("SPEC-20240117-001");
        spec.setSpecName("测试规约");
        
        TestSpecificationResponseDTO responseDTO = new TestSpecificationResponseDTO();
        responseDTO.setId(id);
        responseDTO.setSpecCode("SPEC-20240117-001");
        
        when(specificationService.getSpecificationById(id))
            .thenReturn(spec);
        when(entityDTOMapper.toTestSpecificationResponseDTO(any(TestSpecification.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/specifications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
    
    @Test
    @DisplayName("删除测试规约-成功")
    void testDeleteSpecification_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/specifications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

