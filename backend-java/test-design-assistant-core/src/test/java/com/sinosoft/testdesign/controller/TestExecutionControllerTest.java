package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.TestExecutionTaskRequestDTO;
import com.sinosoft.testdesign.dto.TestExecutionTaskResponseDTO;
import com.sinosoft.testdesign.dto.TestExecutionRecordRequestDTO;
import com.sinosoft.testdesign.dto.TestExecutionRecordResponseDTO;
import com.sinosoft.testdesign.dto.TestExecutionStatisticsDTO;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestExecutionService;
import com.sinosoft.testdesign.service.UIScriptRepairService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试执行管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("测试执行管理Controller测试")
class TestExecutionControllerTest extends BaseControllerTest {
    
    @MockBean
    private TestExecutionService testExecutionService;
    
    @MockBean
    private UIScriptRepairService uiScriptRepairService;
    
    @MockBean
    private EntityDTOMapper entityDTOMapper;
    
    @Test
    @DisplayName("创建执行任务-成功")
    void testCreateExecutionTask_Success() throws Exception {
        // Given
        TestExecutionTaskRequestDTO dto = new TestExecutionTaskRequestDTO();
        dto.setTaskName("测试执行任务");
        dto.setTaskType("AUTOMATED");
        
        TestExecutionTask task = new TestExecutionTask();
        task.setId(1L);
        task.setTaskCode("TASK-20240117-001");
        task.setTaskName("测试执行任务");
        
        TestExecutionTaskResponseDTO responseDTO = new TestExecutionTaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTaskCode("TASK-20240117-001");
        responseDTO.setTaskName("测试执行任务");
        
        when(entityDTOMapper.toTestExecutionTaskEntity(any(TestExecutionTaskRequestDTO.class)))
            .thenReturn(task);
        when(testExecutionService.createExecutionTask(any(TestExecutionTask.class)))
            .thenReturn(task);
        when(entityDTOMapper.toTestExecutionTaskResponseDTO(any(TestExecutionTask.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-execution/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.taskCode").value("TASK-20240117-001"));
    }
    
    @Test
    @DisplayName("查询执行任务列表-成功")
    void testGetExecutionTaskList_Success() throws Exception {
        // Given
        TestExecutionTask task = new TestExecutionTask();
        task.setId(1L);
        task.setTaskCode("TASK-20240117-001");
        task.setTaskName("测试执行任务");
        
        List<TestExecutionTask> tasks = new ArrayList<>();
        tasks.add(task);
        Page<TestExecutionTask> page = new PageImpl<>(tasks, PageRequest.of(0, 10), 1);
        
        TestExecutionTaskResponseDTO responseDTO = new TestExecutionTaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTaskCode("TASK-20240117-001");
        
        when(testExecutionService.getExecutionTaskList(any(), any(), any(), any()))
            .thenReturn(page);
        when(entityDTOMapper.toTestExecutionTaskResponseDTO(any(TestExecutionTask.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-execution/tasks")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
    
    @Test
    @DisplayName("获取执行任务详情-成功")
    void testGetExecutionTaskById_Success() throws Exception {
        // Given
        Long id = 1L;
        TestExecutionTask task = new TestExecutionTask();
        task.setId(id);
        task.setTaskCode("TASK-20240117-001");
        task.setTaskName("测试执行任务");
        
        TestExecutionTaskResponseDTO responseDTO = new TestExecutionTaskResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTaskCode("TASK-20240117-001");
        
        when(testExecutionService.getExecutionTaskById(id))
            .thenReturn(task);
        when(entityDTOMapper.toTestExecutionTaskResponseDTO(any(TestExecutionTask.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(get("/v1/test-execution/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }
    
    @Test
    @DisplayName("创建执行记录-成功")
    void testCreateExecutionRecord_Success() throws Exception {
        // Given
        TestExecutionRecordRequestDTO dto = new TestExecutionRecordRequestDTO();
        dto.setTaskId(1L);
        dto.setCaseId(1L);
        
        TestExecutionRecord record = new TestExecutionRecord();
        record.setId(1L);
        record.setRecordCode("REC-20240117-001");
        
        TestExecutionRecordResponseDTO responseDTO = new TestExecutionRecordResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setRecordCode("REC-20240117-001");
        
        when(entityDTOMapper.toTestExecutionRecordEntity(any(TestExecutionRecordRequestDTO.class)))
            .thenReturn(record);
        when(testExecutionService.createExecutionRecord(any(TestExecutionRecord.class)))
            .thenReturn(record);
        when(entityDTOMapper.toTestExecutionRecordResponseDTO(any(TestExecutionRecord.class)))
            .thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/v1/test-execution/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
    
    @Test
    @DisplayName("获取执行统计信息-成功")
    void testGetExecutionStatistics_Success() throws Exception {
        // Given
        TestExecutionStatisticsDTO statistics = new TestExecutionStatisticsDTO();
        TestExecutionStatisticsDTO.TaskStatistics taskStatistics = new TestExecutionStatisticsDTO.TaskStatistics();
        taskStatistics.setTotalTasks(10L);
        statistics.setTaskStatistics(taskStatistics);
        TestExecutionStatisticsDTO.RecordStatistics recordStatistics = new TestExecutionStatisticsDTO.RecordStatistics();
        recordStatistics.setTotalRecords(100L);
        statistics.setRecordStatistics(recordStatistics);
        
        when(testExecutionService.getExecutionStatistics(any(), any(), any(), any()))
            .thenReturn(statistics);
        
        // When & Then
        mockMvc.perform(get("/v1/test-execution/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskStatistics.totalTasks").value(10L));
    }
}

