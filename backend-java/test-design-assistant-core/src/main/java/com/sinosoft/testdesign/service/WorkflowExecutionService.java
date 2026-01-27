package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.WorkflowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流执行服务接口
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface WorkflowExecutionService {
    
    /**
     * 执行工作流
     */
    WorkflowExecution executeWorkflow(
            Long workflowId,
            Map<String, Object> inputData,
            Long creatorId,
            String creatorName
    );
    
    /**
     * 根据执行ID查询执行记录
     */
    Optional<WorkflowExecution> findByExecutionId(String executionId);
    
    /**
     * 根据工作流ID查询执行记录
     */
    List<WorkflowExecution> findByWorkflowId(Long workflowId);
    
    /**
     * 根据工作流代码查询执行记录
     */
    List<WorkflowExecution> findByWorkflowCode(String workflowCode);
    
    /**
     * 根据状态查询执行记录
     */
    List<WorkflowExecution> findByStatus(String status);
    
    /**
     * 分页查询执行记录
     */
    Page<WorkflowExecution> findAll(Pageable pageable);
    
    /**
     * 取消工作流执行
     */
    WorkflowExecution cancelExecution(String executionId);
    
    /**
     * 查询执行进度
     */
    Map<String, Object> getExecutionProgress(String executionId);
}
