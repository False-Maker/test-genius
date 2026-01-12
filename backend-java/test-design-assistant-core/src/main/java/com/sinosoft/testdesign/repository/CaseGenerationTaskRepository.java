package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.CaseGenerationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用例生成任务数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface CaseGenerationTaskRepository extends JpaRepository<CaseGenerationTask, Long>, 
        JpaSpecificationExecutor<CaseGenerationTask> {
    
    /**
     * 根据任务编码查询
     */
    Optional<CaseGenerationTask> findByTaskCode(String taskCode);
    
    /**
     * 根据需求ID查询任务列表
     */
    List<CaseGenerationTask> findByRequirementId(Long requirementId);
    
    /**
     * 根据任务状态查询任务列表
     */
    List<CaseGenerationTask> findByTaskStatus(String taskStatus);
    
    /**
     * 查询指定前缀的任务编码列表（用于编码生成优化）
     */
    List<CaseGenerationTask> findByTaskCodeStartingWithOrderByIdDesc(String prefix);
}

