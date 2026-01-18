package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.TestReportRequestDTO;
import com.sinosoft.testdesign.dto.TestReportResponseDTO;
import com.sinosoft.testdesign.entity.TestReport;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestReportService;
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
 * 测试报告管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("测试报告管理Controller测试")
class TestReportControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestReportService reportService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("生成测试报告-成功")
    void testGenerateReport_Success() throws Exception {
        // Given
        TestReportRequestDTO dto = new TestReportRequestDTO();
        dto.setReportName("测试报告");
        dto.setReportType("EXECUTION");
        
        TestReport report = new TestReport();
        report.setId(1L);
        report.setReportCode("RPT-20240117-001");
        report.setReportName("测试报告");
        
        TestReportResponseDTO responseDTO = new TestReportResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setReportCode("RPT-20240117-001");
        responseDTO.setReportName("测试报告");
        
        when(entityDTOMapper.toTestReportEntity(any(TestReportRequestDTO.class)))
            .thenReturn(report);
        when(reportService.generateReport(any(TestReport.class)))
            .thenReturn(report);
        when(entityDTOMapper.toTestReportResponseDTO(any(TestReport.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.reportCode").value("RPT-20240117-001"));
    }
    
    @Test
    @DisplayName("查询报告列表-成功")
    void testGetReportList_Success() throws Exception {
        // Given
        TestReport report = new TestReport();
        report.setId(1L);
        report.setReportCode("RPT-20240117-001");
        report.setReportName("测试报告");
        
        List<TestReport> reports = new ArrayList<>();
        reports.add(report);
        Page<TestReport> page = new PageImpl<>(reports, PageRequest.of(0, 10), 1);
        
        TestReportResponseDTO responseDTO = new TestReportResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setReportCode("RPT-20240117-001");
        
        when(reportService.getReportList(any()))
            .thenReturn(page);
        when(entityDTOMapper.toTestReportResponseDTO(any(TestReport.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-reports")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取报告详情-成功")
    void testGetReportById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestReport report = new TestReport();
        report.setId(id);
        report.setReportCode("RPT-20240117-001");
        report.setReportName("测试报告");
        
        TestReportResponseDTO responseDTO = new TestReportResponseDTO();
        responseDTO.setId(id);
        responseDTO.setReportCode("RPT-20240117-001");
        
        when(reportService.getReportById(id))
            .thenReturn(report);
        when(entityDTOMapper.toTestReportResponseDTO(any(TestReport.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-reports/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
    
    @Test
    @DisplayName("发布报告-成功")
    void testPublishReport_Success() throws Exception {
        // Given
        Long id = 1L;
        TestReport report = new TestReport();
        report.setId(id);
        report.setReportCode("RPT-20240117-001");
        report.setReportStatus("PUBLISHED");
        
        TestReportResponseDTO responseDTO = new TestReportResponseDTO();
        responseDTO.setId(id);
        responseDTO.setReportStatus("PUBLISHED");
        
        when(reportService.publishReport(id))
            .thenReturn(report);
        when(entityDTOMapper.toTestReportResponseDTO(any(TestReport.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(put("/v1/test-reports/{id}/publish", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reportStatus").value("PUBLISHED"));
    }
    
    @Test
    @DisplayName("删除报告-成功")
    void testDeleteReport_Success() throws Exception {
        // Given
        Long id = 1L;
        
        // When & Then
        mockMvc.perform(delete("/v1/test-reports/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

