package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.TestExecutionStatisticsDTO;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import com.sinosoft.testdesign.repository.TestExecutionTaskRepository;
import com.sinosoft.testdesign.repository.TestExecutionRecordRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试执行服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试执行服务测试")
class TestExecutionServiceImplTest {
    
    @Mock
    private TestExecutionTaskRepository taskRepository;
    
    @Mock
    private TestExecutionRecordRepository recordRepository;
    
    @InjectMocks
    private TestExecutionServiceImpl executionService;
    
    private TestExecutionTask testTask;
    private TestExecutionRecord testRecord;
    
    @BeforeEach
    void setUp() {
        testTask = new TestExecutionTask();
        testTask.setId(1L);
        testTask.setTaskCode("TASK-20240117-001");
        testTask.setTaskName("测试执行任务");
        testTask.setTaskType("MANUAL_EXECUTION");
        testTask.setTaskStatus("PENDING");
        testTask.setProgress(0);
        testTask.setRequirementId(1L);
        testTask.setCaseId(1L);
        
        testRecord = new TestExecutionRecord();
        testRecord.setId(1L);
        testRecord.setRecordCode("REC-20240117-001");
        testRecord.setTaskId(1L);
        testRecord.setCaseId(1L);
        testRecord.setExecutionType("MANUAL");
        testRecord.setExecutionStatus("PENDING");
    }
    
    @Test
    @DisplayName("创建执行任务-成功")
    void testCreateExecutionTask_Success() {
        // Given
        TestExecutionTask newTask = new TestExecutionTask();
        newTask.setTaskName("新执行任务");
        newTask.setTaskType("MANUAL_EXECUTION");
        
        when(taskRepository.findByTaskCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(taskRepository.save(any(TestExecutionTask.class)))
            .thenAnswer(invocation -> {
                TestExecutionTask task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });
        
        // When
        TestExecutionTask result = executionService.createExecutionTask(newTask);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getTaskCode());
        assertTrue(result.getTaskCode().startsWith("TASK-"));
        assertEquals("PENDING", result.getTaskStatus());
        assertEquals(0, result.getProgress());
        verify(taskRepository, times(1)).save(any(TestExecutionTask.class));
    }
    
    @Test
    @DisplayName("创建执行任务-任务名称为空")
    void testCreateExecutionTask_TaskNameEmpty() {
        // Given
        TestExecutionTask newTask = new TestExecutionTask();
        newTask.setTaskName("");
        newTask.setTaskType("MANUAL_EXECUTION");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            executionService.createExecutionTask(newTask);
        });
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建执行任务-任务类型为空")
    void testCreateExecutionTask_TaskTypeEmpty() {
        // Given
        TestExecutionTask newTask = new TestExecutionTask();
        newTask.setTaskName("新执行任务");
        newTask.setTaskType("");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            executionService.createExecutionTask(newTask);
        });
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建执行任务-编码已存在")
    void testCreateExecutionTask_CodeExists() {
        // Given
        TestExecutionTask newTask = new TestExecutionTask();
        newTask.setTaskCode("TASK-20240117-001");
        newTask.setTaskName("新执行任务");
        newTask.setTaskType("MANUAL_EXECUTION");
        
        when(taskRepository.findByTaskCode("TASK-20240117-001"))
            .thenReturn(Optional.of(testTask));
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            executionService.createExecutionTask(newTask);
        });
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新执行任务-成功")
    void testUpdateExecutionTask_Success() {
        // Given
        TestExecutionTask updateTask = new TestExecutionTask();
        updateTask.setTaskName("更新后的任务名称");
        updateTask.setTaskType("AUTO_SCRIPT_GENERATION");
        
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(TestExecutionTask.class)))
            .thenReturn(testTask);
        
        // When
        TestExecutionTask result = executionService.updateExecutionTask(1L, updateTask);
        
        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(TestExecutionTask.class));
    }
    
    @Test
    @DisplayName("更新执行任务-任务不存在")
    void testUpdateExecutionTask_NotFound() {
        // Given
        TestExecutionTask updateTask = new TestExecutionTask();
        updateTask.setTaskName("更新后的任务名称");
        
        when(taskRepository.findById(1L))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            executionService.updateExecutionTask(1L, updateTask);
        });
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新执行任务-不允许修改编码")
    void testUpdateExecutionTask_CannotModifyCode() {
        // Given
        TestExecutionTask updateTask = new TestExecutionTask();
        updateTask.setTaskCode("TASK-20240117-999");
        
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            executionService.updateExecutionTask(1L, updateTask);
        });
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询执行任务-根据ID")
    void testGetExecutionTaskById_Success() {
        // Given
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        
        // When
        TestExecutionTask result = executionService.getExecutionTaskById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TASK-20240117-001", result.getTaskCode());
    }
    
    @Test
    @DisplayName("查询执行任务-根据编码")
    void testGetExecutionTaskByCode_Success() {
        // Given
        when(taskRepository.findByTaskCode("TASK-20240117-001"))
            .thenReturn(Optional.of(testTask));
        
        // When
        TestExecutionTask result = executionService.getExecutionTaskByCode("TASK-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("TASK-20240117-001", result.getTaskCode());
    }
    
    @Test
    @DisplayName("分页查询执行任务列表")
    void testGetExecutionTaskList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestExecutionTask> tasks = new ArrayList<>();
        tasks.add(testTask);
        Page<TestExecutionTask> page = new PageImpl<>(tasks, pageable, 1);
        
        when(taskRepository.findWithFilters(any(), any(), any(), eq(pageable)))
            .thenReturn(page);
        
        // When
        Page<TestExecutionTask> result = executionService.getExecutionTaskList(pageable, null, null, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findWithFilters(any(), any(), any(), eq(pageable));
    }
    
    @Test
    @DisplayName("更新任务状态-成功")
    void testUpdateTaskStatus_Success() {
        // Given
        when(taskRepository.findByTaskCode("TASK-20240117-001"))
            .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(TestExecutionTask.class)))
            .thenReturn(testTask);
        
        // When
        TestExecutionTask result = executionService.updateTaskStatus("TASK-20240117-001", "PROCESSING");
        
        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(TestExecutionTask.class));
    }
    
    @Test
    @DisplayName("删除执行任务-成功")
    void testDeleteExecutionTask_Success() {
        // Given
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        when(recordRepository.findByTaskId(1L))
            .thenReturn(new ArrayList<>());
        doNothing().when(taskRepository).delete(any(TestExecutionTask.class));
        
        // When
        executionService.deleteExecutionTask(1L);
        
        // Then
        verify(taskRepository, times(1)).findById(1L);
        verify(recordRepository, times(1)).findByTaskId(1L);
        verify(taskRepository, times(1)).delete(any(TestExecutionTask.class));
    }
    
    @Test
    @DisplayName("创建执行记录-成功")
    void testCreateExecutionRecord_Success() {
        // Given
        TestExecutionRecord newRecord = new TestExecutionRecord();
        newRecord.setTaskId(1L);
        newRecord.setCaseId(1L);
        newRecord.setExecutionType("MANUAL");
        newRecord.setExecutionStatus("PENDING");
        
        when(taskRepository.findById(1L))
            .thenReturn(Optional.of(testTask));
        when(recordRepository.findByRecordCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(recordRepository.save(any(TestExecutionRecord.class)))
            .thenAnswer(invocation -> {
                TestExecutionRecord record = invocation.getArgument(0);
                record.setId(1L);
                return record;
            });
        
        // When
        TestExecutionRecord result = executionService.createExecutionRecord(newRecord);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getRecordCode());
        assertTrue(result.getRecordCode().startsWith("REC-"));
        verify(recordRepository, times(1)).save(any(TestExecutionRecord.class));
    }
    
    @Test
    @DisplayName("查询执行记录-根据任务ID")
    void testGetExecutionRecordsByTaskId_Success() {
        // Given
        List<TestExecutionRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(recordRepository.findByTaskId(1L))
            .thenReturn(records);
        
        // When
        List<TestExecutionRecord> result = executionService.getExecutionRecordsByTaskId(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTaskId());
    }
    
    @Test
    @DisplayName("更新任务进度-成功")
    void testUpdateTaskProgress_Success() {
        // Given
        when(taskRepository.findByTaskCode("TASK-20240117-001"))
            .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(TestExecutionTask.class)))
            .thenReturn(testTask);
        
        // When
        TestExecutionTask result = executionService.updateTaskProgress("TASK-20240117-001", 50);
        
        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(TestExecutionTask.class));
    }
    
    @Test
    @DisplayName("获取执行统计信息-成功")
    void testGetExecutionStatistics_Success() {
        // Given
        List<TestExecutionTask> tasks = new ArrayList<>();
        tasks.add(testTask);
        
        List<TestExecutionRecord> records = new ArrayList<>();
        testRecord.setExecutionStatus("SUCCESS");
        records.add(testRecord);
        
        when(taskRepository.findAll())
            .thenReturn(tasks);
        when(recordRepository.findAll())
            .thenReturn(records);
        
        // When
        TestExecutionStatisticsDTO result = executionService.getExecutionStatistics(null, null, null, null);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getTaskStatistics());
        assertNotNull(result.getRecordStatistics());
        assertTrue(result.getTaskStatistics().getTotalTasks() >= 0);
        assertTrue(result.getRecordStatistics().getTotalRecords() >= 0);
        verify(taskRepository, atLeastOnce()).findAll();
        verify(recordRepository, atLeastOnce()).findAll();
    }
}

