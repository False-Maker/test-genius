package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.WorkflowVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工作流版本Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, Long>, JpaSpecificationExecutor<WorkflowVersion> {
    
    /**
     * 根据工作流ID查询所有版本
     */
    List<WorkflowVersion> findByWorkflowIdOrderByVersionDesc(Long workflowId);
    
    /**
     * 根据工作流代码查询所有版本
     */
    List<WorkflowVersion> findByWorkflowCodeOrderByVersionDesc(String workflowCode);
    
    /**
     * 根据工作流ID和版本号查询
     */
    Optional<WorkflowVersion> findByWorkflowIdAndVersion(Long workflowId, Integer version);
    
    /**
     * 根据工作流代码和版本号查询
     */
    Optional<WorkflowVersion> findByWorkflowCodeAndVersion(String workflowCode, Integer version);
}
