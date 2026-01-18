package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.BatchCaseGenerationRequest;
import com.sinosoft.testdesign.dto.BatchCaseGenerationResult;
import com.sinosoft.testdesign.dto.CaseGenerationRequest;
import com.sinosoft.testdesign.dto.CaseGenerationResult;
import com.sinosoft.testdesign.dto.GenerationTaskDTO;
import com.sinosoft.testdesign.service.IntelligentCaseGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用例生成Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("用例生成Controller测试")
class CaseGenerationControllerTest extends BaseControllerTest {
    
    @MockBean
    private IntelligentCaseGenerationService caseGenerationService;
    
    @Test
    @DisplayName("生成用例-成功")
    void testGenerateTestCases_Success() throws Exception {
        // Given
        CaseGenerationRequest request = new CaseGenerationRequest();
        request.setRequirementId(1L);
        request.setLayerCode("LAYER-001");
        request.setMethodCode("METHOD-001");
        
        CaseGenerationResult result = new CaseGenerationResult();
        result.setTaskId(1L);
        result.setStatus("PENDING");
        result.setMessage("任务已创建");
        
        when(caseGenerationService.generateTestCases(any(CaseGenerationRequest.class)))
            .thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/v1/case-generation/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value(1L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
    
    @Test
    @DisplayName("批量生成用例-成功")
    void testBatchGenerateTestCases_Success() throws Exception {
        // Given
        BatchCaseGenerationRequest request = new BatchCaseGenerationRequest();
        List<Long> requirementIds = new ArrayList<>();
        requirementIds.add(1L);
        requirementIds.add(2L);
        request.setRequirementIds(requirementIds);
        request.setLayerCode("LAYER-001");
        request.setMethodCode("METHOD-001");
        
        BatchCaseGenerationResult result = new BatchCaseGenerationResult();
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(1L);
        taskIds.add(2L);
        result.setTaskIds(taskIds);
        result.setTotalTasks(2);
        
        when(caseGenerationService.batchGenerateTestCases(any(BatchCaseGenerationRequest.class)))
            .thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/v1/case-generation/batch-generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTasks").value(2))
                .andExpect(jsonPath("$.data.taskIds").isArray())
                .andExpect(jsonPath("$.data.taskIds.length()").value(2));
    }
    
    @Test
    @DisplayName("查询生成任务-成功")
    void testGetGenerationTask_Success() throws Exception {
        // Given
        Long taskId = 1L;
        GenerationTaskDTO task = new GenerationTaskDTO();
        task.setId(taskId);
        task.setTaskCode("TASK-20240101-001");
        task.setStatus("SUCCESS");
        task.setProgress(100);
        
        when(caseGenerationService.getGenerationTask(taskId))
            .thenReturn(task);
        
        // When & Then
        mockMvc.perform(get("/v1/case-generation/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.taskCode").value("TASK-20240101-001"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.progress").value(100));
    }
    
    @Test
    @DisplayName("批量查询生成任务-成功")
    void testGetBatchGenerationTasks_Success() throws Exception {
        // Given
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(1L);
        taskIds.add(2L);
        
        List<GenerationTaskDTO> tasks = new ArrayList<>();
        GenerationTaskDTO task1 = new GenerationTaskDTO();
        task1.setId(1L);
        task1.setTaskCode("TASK-20240101-001");
        task1.setStatus("SUCCESS");
        tasks.add(task1);
        
        GenerationTaskDTO task2 = new GenerationTaskDTO();
        task2.setId(2L);
        task2.setTaskCode("TASK-20240101-002");
        task2.setStatus("PROCESSING");
        tasks.add(task2);
        
        when(caseGenerationService.getBatchGenerationTasks(anyList()))
            .thenReturn(tasks);
        
        // When & Then
        mockMvc.perform(post("/v1/case-generation/batch-query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[1].id").value(2L));
    }
}

