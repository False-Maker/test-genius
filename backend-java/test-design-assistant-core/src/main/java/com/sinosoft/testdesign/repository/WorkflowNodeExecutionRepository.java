package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.WorkflowNodeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流节点执行记录Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface WorkflowNodeExecutionRepository extends JpaRepository<WorkflowNodeExecution, Long>, JpaSpecificationExecutor<WorkflowNodeExecution> {
    
    /**
     * 根据执行ID查询所有节点执行记录
     */
    List<WorkflowNodeExecution> findByExecutionIdOrderByCreateTimeAsc(String executionId);
    
    /**
     * 根据执行ID和节点ID查询
     */
    List<WorkflowNodeExecution> findByExecutionIdAndNodeId(String executionId, String nodeId);
}
