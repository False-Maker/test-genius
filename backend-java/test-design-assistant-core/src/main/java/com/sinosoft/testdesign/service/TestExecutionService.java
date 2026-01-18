package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 测试执行服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface TestExecutionService {
    
    /**
     * 创建执行任务
     */
    TestExecutionTask createExecutionTask(TestExecutionTask task);
    
    /**
     * 更新执行任务
     */
    TestExecutionTask updateExecutionTask(Long id, TestExecutionTask task);
    
    /**
     * 根据ID查询执行任务
     */
    TestExecutionTask getExecutionTaskById(Long id);
    
    /**
     * 根据任务编码查询执行任务
     */
    TestExecutionTask getExecutionTaskByCode(String taskCode);
    
    /**
     * 分页查询执行任务列表
     */
    Page<TestExecutionTask> getExecutionTaskList(Pageable pageable, String taskName, String taskStatus, String taskType);
    
    /**
     * 更新任务状态
     */
    TestExecutionTask updateTaskStatus(String taskCode, String status);
    
    /**
     * 删除执行任务
     */
    void deleteExecutionTask(Long id);
    
    /**
     * 创建执行记录
     */
    TestExecutionRecord createExecutionRecord(TestExecutionRecord record);
    
    /**
     * 根据ID查询执行记录
     */
    TestExecutionRecord getExecutionRecordById(Long id);
    
    /**
     * 根据记录编码查询执行记录
     */
    TestExecutionRecord getExecutionRecordByCode(String recordCode);
    
    /**
     * 根据任务ID查询执行记录列表
     */
    java.util.List<TestExecutionRecord> getExecutionRecordsByTaskId(Long taskId);
    
    /**
     * 分页查询执行记录列表
     */
    Page<TestExecutionRecord> getExecutionRecordList(Pageable pageable, Long taskId, Long caseId, String executionStatus);
    
    /**
     * 更新执行记录状态
     */
    TestExecutionRecord updateExecutionRecordStatus(String recordCode, String status);
    
    /**
     * 更新任务进度
     */
    TestExecutionTask updateTaskProgress(String taskCode, Integer progress);
    
    /**
     * 获取执行统计信息
     */
    com.sinosoft.testdesign.dto.TestExecutionStatisticsDTO getExecutionStatistics(
            Long requirementId, Long caseId, String startDate, String endDate);
}

