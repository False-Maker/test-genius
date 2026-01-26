package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.EquivalenceTableRequestDTO;
import com.sinosoft.testdesign.dto.EquivalenceTableResponseDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableRequestDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableResponseDTO;
import com.sinosoft.testdesign.service.DataDocumentGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据文档生成Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("数据文档生成Controller测试")
class DataDocumentControllerTest extends BaseControllerTest {
    
    @MockBean
    private DataDocumentGenerationService dataDocumentGenerationService;
    
    @Test
    @DisplayName("生成等价类表-成功")
    void testGenerateEquivalenceTable_Success() throws Exception {
        // Given
        EquivalenceTableRequestDTO request = new EquivalenceTableRequestDTO();
        request.setRequirementId(1L);
        request.setTitle("等价类表");
        
        EquivalenceTableResponseDTO response = new EquivalenceTableResponseDTO();
        response.setTitle("等价类表");
        Map<String, Object> row = new HashMap<>();
        row.put("参数1", "值1");
        row.put("参数2", "值2");
        response.setTableData(Arrays.asList(row));
        
        when(dataDocumentGenerationService.generateEquivalenceTable(any(EquivalenceTableRequestDTO.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/v1/data-documents/equivalence-tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("等价类表"));
    }
    
    @Test
    @DisplayName("生成正交表-成功")
    void testGenerateOrthogonalTable_Success() throws Exception {
        // Given
        OrthogonalTableRequestDTO request = new OrthogonalTableRequestDTO();
        request.setTitle("正交表");
        Map<String, List<String>> factors = new HashMap<>();
        factors.put("浏览器", Arrays.asList("Chrome", "Firefox"));
        factors.put("操作系统", Arrays.asList("Windows", "Mac"));
        request.setFactors(factors);
        
        OrthogonalTableResponseDTO response = new OrthogonalTableResponseDTO();
        response.setTitle("正交表");
        Map<String, Object> row = new HashMap<>();
        row.put("浏览器", "Chrome");
        row.put("操作系统", "Windows");
        response.setTableData(Arrays.asList(row));
        
        when(dataDocumentGenerationService.generateOrthogonalTable(any(OrthogonalTableRequestDTO.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/v1/data-documents/orthogonal-tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("正交表"));
    }
}

