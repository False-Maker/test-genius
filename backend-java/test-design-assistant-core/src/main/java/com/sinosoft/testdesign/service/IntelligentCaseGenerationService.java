package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.TestCase;

import java.util.List;

/**
 * 智能用例生成服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface IntelligentCaseGenerationService {
    
    /**
     * 生成测试用例（异步）
     * @param request 用例生成请求
     * @return 用例生成结果（包含任务ID）
     */
    CaseGenerationResult generateTestCases(CaseGenerationRequest request);
    
    /**
     * 批量生成测试用例（异步）
     * @param request 批量用例生成请求
     * @return 批量用例生成结果（包含任务ID列表）
     */
    BatchCaseGenerationResult batchGenerateTestCases(BatchCaseGenerationRequest request);
    
    /**
     * 查询生成任务状态
     * @param taskId 任务ID
     * @return 任务信息
     */
    GenerationTaskDTO getGenerationTask(Long taskId);
    
    /**
     * 批量查询生成任务状态
     * @param taskIds 任务ID列表
     * @return 任务信息列表
     */
    List<GenerationTaskDTO> getBatchGenerationTasks(List<Long> taskIds);
    
    /**
     * 异步执行用例生成任务
     * @param taskId 任务ID
     */
    void executeGenerationTask(Long taskId);

    /**
     * 查询用例生成任务列表（分页）
     * @param query 查询条件
     * @return 任务列表
     */
    PageResult<TaskListDTO> getTaskList(TaskListQueryDTO query);

    /**
     * 查询用例生成任务详情
     * @param taskId 任务ID
     * @return 任务详情（包含生成的用例列表）
     */
    TaskDetailDTO getTaskDetail(Long taskId);
}

