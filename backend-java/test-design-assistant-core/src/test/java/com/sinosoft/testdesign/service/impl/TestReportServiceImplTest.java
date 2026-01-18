package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.repository.*;
import com.sinosoft.testdesign.service.TestReportTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试报告生成服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试报告生成服务测试")
class TestReportServiceImplTest {
    
    @Mock
    private TestReportRepository reportRepository;
    
    @Mock
    private TestReportTemplateRepository templateRepository;
    
    @Mock
    private TestReportTemplateService templateService;
    
    @Mock
    private TestExecutionTaskRepository taskRepository;
    
    @Mock
    private TestExecutionRecordRepository recordRepository;
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @InjectMocks
    private TestReportServiceImpl reportService;
    
    private TestReport testReport;
    private TestExecutionTask testTask;
    private TestExecutionRecord testRecord;
    
    @BeforeEach
    void setUp() {
        // 设置basePath
        ReflectionTestUtils.setField(reportService, "basePath", "./test-uploads");
        
        testReport = new TestReport();
        testReport.setId(1L);
        testReport.setReportCode("RPT-20240117-001");
        testReport.setReportName("测试报告");
        testReport.setReportType("EXECUTION");
        testReport.setReportStatus("DRAFT");
        testReport.setRequirementId(1L);
        testReport.setExecutionTaskId(1L);
        
        testTask = new TestExecutionTask();
        testTask.setId(1L);
        testTask.setTaskCode("TASK-20240117-001");
        testTask.setTaskName("测试执行任务");
        testTask.setTaskStatus("SUCCESS");
        
        testRecord = new TestExecutionRecord();
        testRecord.setId(1L);
        testRecord.setRecordCode("REC-20240117-001");
        testRecord.setTaskId(1L);
        testRecord.setCaseId(1L);
        testRecord.setExecutionStatus("SUCCESS");
    }
    
    @Test
    @DisplayName("生成测试报告-成功")
    void testGenerateReport_Success() {
        // Given
        TestReport newReport = new TestReport();
        newReport.setReportName("新测试报告");
        newReport.setReportType("EXECUTION");
        newReport.setRequirementId(1L);
        
        when(reportRepository.findByReportCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(reportRepository.save(any(TestReport.class)))
            .thenAnswer(invocation -> {
                TestReport report = invocation.getArgument(0);
                report.setId(1L);
                return report;
            });
        when(taskRepository.findByRequirementId(anyLong()))
            .thenReturn(new ArrayList<>());
        
        // When
        TestReport result = reportService.generateReport(newReport);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getReportCode());
        assertTrue(result.getReportCode().startsWith("RPT-"));
        assertEquals("DRAFT", result.getReportStatus());
        verify(reportRepository, times(1)).save(any(TestReport.class));
    }
    
    @Test
    @DisplayName("生成测试报告-报告名称为空")
    void testGenerateReport_ReportNameEmpty() {
        // Given
        TestReport newReport = new TestReport();
        newReport.setReportName("");
        newReport.setReportType("EXECUTION");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            reportService.generateReport(newReport);
        });
        verify(reportRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("生成测试报告-报告类型为空")
    void testGenerateReport_ReportTypeEmpty() {
        // Given
        TestReport newReport = new TestReport();
        newReport.setReportName("新测试报告");
        newReport.setReportType("");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            reportService.generateReport(newReport);
        });
        verify(reportRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询报告-根据编码")
    void testGetReportByCode_Success() {
        // Given
        when(reportRepository.findByReportCode("RPT-20240117-001"))
            .thenReturn(Optional.of(testReport));
        
        // When
        TestReport result = reportService.getReportByCode("RPT-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("RPT-20240117-001", result.getReportCode());
    }
    
    @Test
    @DisplayName("查询报告-根据ID")
    void testGetReportById_Success() {
        // Given
        when(reportRepository.findById(1L))
            .thenReturn(Optional.of(testReport));
        
        // When
        TestReport result = reportService.getReportById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
    
    @Test
    @DisplayName("分页查询报告列表")
    void testGetReportList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestReport> reports = new ArrayList<>();
        reports.add(testReport);
        Page<TestReport> page = new PageImpl<>(reports, pageable, 1);
        
        when(reportRepository.findAll(pageable))
            .thenReturn(page);
        
        // When
        Page<TestReport> result = reportService.getReportList(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("根据需求ID查询报告列表")
    void testGetReportsByRequirementId_Success() {
        // Given
        List<TestReport> reports = new ArrayList<>();
        reports.add(testReport);
        
        when(reportRepository.findByRequirementId(1L))
            .thenReturn(reports);
        
        // When
        List<TestReport> result = reportService.getReportsByRequirementId(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getRequirementId());
    }
    
    @Test
    @DisplayName("更新报告-成功")
    void testUpdateReport_Success() {
        // Given
        TestReport updateReport = new TestReport();
        updateReport.setReportName("更新后的报告名称");
        
        when(reportRepository.findById(1L))
            .thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(TestReport.class)))
            .thenReturn(testReport);
        
        // When
        TestReport result = reportService.updateReport(1L, updateReport);
        
        // Then
        assertNotNull(result);
        verify(reportRepository, times(1)).save(any(TestReport.class));
    }
    
    @Test
    @DisplayName("更新报告-报告不存在")
    void testUpdateReport_NotFound() {
        // Given
        TestReport updateReport = new TestReport();
        updateReport.setReportName("更新后的报告名称");
        
        when(reportRepository.findById(1L))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            reportService.updateReport(1L, updateReport);
        });
        verify(reportRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("发布报告-成功")
    void testPublishReport_Success() {
        // Given
        when(reportRepository.findById(1L))
            .thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(TestReport.class)))
            .thenReturn(testReport);
        
        // When
        TestReport result = reportService.publishReport(1L);
        
        // Then
        assertNotNull(result);
        verify(reportRepository, times(1)).save(any(TestReport.class));
    }
    
    @Test
    @DisplayName("删除报告-成功")
    void testDeleteReport_Success() {
        // Given
        when(reportRepository.findById(1L))
            .thenReturn(Optional.of(testReport));
        doNothing().when(reportRepository).delete(any(TestReport.class));
        
        // When
        reportService.deleteReport(1L);
        
        // Then
        verify(reportRepository, times(1)).delete(any(TestReport.class));
    }
    
    @Test
    @DisplayName("汇总测试执行结果-成功")
    void testSummarizeExecutionResults_Success() {
        // Given
        List<TestExecutionRecord> records = new ArrayList<>();
        testRecord.setExecutionStatus("SUCCESS");
        records.add(testRecord);
        
        List<TestExecutionTask> tasks = new ArrayList<>();
        TestExecutionTask task = new TestExecutionTask();
        task.setId(1L);
        tasks.add(task);
        
        when(taskRepository.findByRequirementId(1L))
            .thenReturn(tasks);
        when(recordRepository.findByTaskId(1L))
            .thenReturn(records);
        
        // When
        String result = reportService.summarizeExecutionResults(1L, null);
        
        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).findByRequirementId(1L);
        verify(recordRepository, times(1)).findByTaskId(1L);
    }
}

