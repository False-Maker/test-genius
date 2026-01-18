package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.PathDiagramRequestDTO;
import com.sinosoft.testdesign.dto.PathDiagramResponseDTO;
import com.sinosoft.testdesign.dto.SceneDiagramRequestDTO;
import com.sinosoft.testdesign.dto.SceneDiagramResponseDTO;
import com.sinosoft.testdesign.service.FlowDocumentGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 流程文档生成Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("流程文档生成Controller测试")
class FlowDocumentControllerTest extends BaseControllerTest {
    
    @MockBean
    private FlowDocumentGenerationService flowDocumentGenerationService;
    
    @Test
    @DisplayName("生成场景图-成功")
    void testGenerateSceneDiagram_Success() throws Exception {
        // Given
        SceneDiagramRequestDTO request = new SceneDiagramRequestDTO();
        request.setRequirementId(1L);
        request.setTitle("测试场景图");
        
        SceneDiagramResponseDTO response = new SceneDiagramResponseDTO();
        response.setTitle("测试场景图");
        response.setMermaidCode("graph TD\nA[开始] --> B[结束]");
        
        when(flowDocumentGenerationService.generateSceneDiagram(any(SceneDiagramRequestDTO.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/flow-documents/scene-diagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试场景图"))
                .andExpect(jsonPath("$.data.mermaidCode").exists());
    }
    
    @Test
    @DisplayName("生成路径图-成功")
    void testGeneratePathDiagram_Success() throws Exception {
        // Given
        PathDiagramRequestDTO request = new PathDiagramRequestDTO();
        request.setCaseId(1L);
        request.setTitle("测试路径图");
        
        PathDiagramResponseDTO response = new PathDiagramResponseDTO();
        response.setTitle("测试路径图");
        response.setMermaidCode("graph LR\nA[步骤1] --> B[步骤2]");
        
        when(flowDocumentGenerationService.generatePathDiagram(any(PathDiagramRequestDTO.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/v1/flow-documents/path-diagrams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试路径图"))
                .andExpect(jsonPath("$.data.mermaidCode").exists());
    }
    
    @Test
    @DisplayName("导出场景图文件-成功")
    void testExportSceneDiagramFile_Success() throws Exception {
        // Given
        String mermaidCode = "graph TD\nA[开始] --> B[结束]";
        String format = "PNG";
        String fileUrl = "http://example.com/files/scene_diagram.png";
        
        when(flowDocumentGenerationService.exportSceneDiagramFile(eq(mermaidCode), eq(format), anyString()))
            .thenReturn(fileUrl);
        
        // When & Then
        mockMvc.perform(post("/api/v1/flow-documents/scene-diagrams/export")
                .param("mermaidCode", mermaidCode)
                .param("format", format))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(fileUrl));
    }
}

