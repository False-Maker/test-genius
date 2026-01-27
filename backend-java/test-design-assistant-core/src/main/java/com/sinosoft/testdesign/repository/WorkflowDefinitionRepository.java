package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工作流定义Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long>, JpaSpecificationExecutor<WorkflowDefinition> {
    
    /**
     * 根据工作流代码查询
     */
    Optional<WorkflowDefinition> findByWorkflowCode(String workflowCode);
    
    /**
     * 根据工作流类型查询
     */
    List<WorkflowDefinition> findByWorkflowTypeOrderByCreateTimeDesc(String workflowType);
    
    /**
     * 查询所有启用的工作流
     */
    List<WorkflowDefinition> findByIsActiveTrueOrderByCreateTimeDesc();
    
    /**
     * 根据工作流类型查询启用的工作流
     */
    List<WorkflowDefinition> findByWorkflowTypeAndIsActiveTrueOrderByCreateTimeDesc(String workflowType);
    
    /**
     * 查询默认工作流
     */
    Optional<WorkflowDefinition> findByWorkflowTypeAndIsDefaultTrueAndIsActiveTrue(String workflowType);
}
