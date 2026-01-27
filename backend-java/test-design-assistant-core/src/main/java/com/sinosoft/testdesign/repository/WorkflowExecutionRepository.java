package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.WorkflowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 工作流执行记录Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long>, JpaSpecificationExecutor<WorkflowExecution> {
    
    /**
     * 根据执行ID查询
     */
    Optional<WorkflowExecution> findByExecutionId(String executionId);
    
    /**
     * 根据工作流ID查询执行记录
     */
    List<WorkflowExecution> findByWorkflowIdOrderByCreateTimeDesc(Long workflowId);
    
    /**
     * 根据工作流代码查询执行记录
     */
    List<WorkflowExecution> findByWorkflowCodeOrderByCreateTimeDesc(String workflowCode);
    
    /**
     * 根据状态查询执行记录
     */
    List<WorkflowExecution> findByStatusOrderByCreateTimeDesc(String status);
    
    /**
     * 根据时间范围查询执行记录
     */
    List<WorkflowExecution> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询执行记录
     */
    Page<WorkflowExecution> findAll(Pageable pageable);
}
