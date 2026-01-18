package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.PageElementInfoRequestDTO;
import com.sinosoft.testdesign.dto.PageElementInfoResponseDTO;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.PageElementService;
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
 * 页面元素信息Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("页面元素信息Controller测试")
class PageElementControllerTest extends BaseControllerTest {
    
    @MockBean
    private PageElementService pageElementService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建页面元素信息-成功")
    void testCreatePageElement_Success() throws Exception {
        // Given
        PageElementInfoRequestDTO dto = new PageElementInfoRequestDTO();
        dto.setPageUrl("http://example.com/page");
        dto.setElementType("BUTTON");
        dto.setElementText("提交按钮");
        
        PageElementInfo element = new PageElementInfo();
        element.setId(1L);
        element.setElementCode("ELE-20240117-001");
        element.setPageUrl("http://example.com/page");
        
        PageElementInfoResponseDTO responseDTO = new PageElementInfoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setElementCode("ELE-20240117-001");
        responseDTO.setPageUrl("http://example.com/page");
        
        when(entityDTOMapper.toPageElementInfoEntity(any(PageElementInfoRequestDTO.class)))
            .thenReturn(element);
        when(pageElementService.createPageElement(any(PageElementInfo.class)))
            .thenReturn(element);
        when(entityDTOMapper.toPageElementInfoResponseDTO(any(PageElementInfo.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/page-elements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.elementCode").value("ELE-20240117-001"));
    }
    
    @Test
    @DisplayName("查询页面元素信息列表-成功")
    void testGetPageElementList_Success() throws Exception {
        // Given
        PageElementInfo element = new PageElementInfo();
        element.setId(1L);
        element.setElementCode("ELE-20240117-001");
        element.setPageUrl("http://example.com/page");
        
        List<PageElementInfo> elements = new ArrayList<>();
        elements.add(element);
        Page<PageElementInfo> page = new PageImpl<>(elements, PageRequest.of(0, 10), 1);
        
        PageElementInfoResponseDTO responseDTO = new PageElementInfoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setElementCode("ELE-20240117-001");
        
        when(pageElementService.getPageElementList(any(), any(), any()))
            .thenReturn(page);
        when(entityDTOMapper.toPageElementInfoResponseDTO(any(PageElementInfo.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/page-elements")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取页面元素信息详情-成功")
    void testGetPageElementById_Success() throws Exception {
        // Given
        Long id = 1L;
        PageElementInfo element = new PageElementInfo();
        element.setId(id);
        element.setElementCode("ELE-20240117-001");
        
        PageElementInfoResponseDTO responseDTO = new PageElementInfoResponseDTO();
        responseDTO.setId(id);
        responseDTO.setElementCode("ELE-20240117-001");
        
        when(pageElementService.getPageElementById(id))
            .thenReturn(element);
        when(entityDTOMapper.toPageElementInfoResponseDTO(any(PageElementInfo.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/page-elements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
    
    @Test
    @DisplayName("删除页面元素信息-成功")
    void testDeletePageElement_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/page-elements/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

