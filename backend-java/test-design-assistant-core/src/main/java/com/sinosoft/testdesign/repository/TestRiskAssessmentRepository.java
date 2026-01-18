package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestRiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 风险评估数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface TestRiskAssessmentRepository extends JpaRepository<TestRiskAssessment, Long>,
        JpaSpecificationExecutor<TestRiskAssessment> {
    
    /**
     * 根据评估编码查询
     */
    Optional<TestRiskAssessment> findByAssessmentCode(String assessmentCode);
    
    /**
     * 根据需求ID查询风险评估列表
     */
    List<TestRiskAssessment> findByRequirementId(Long requirementId);
    
    /**
     * 根据执行任务ID查询风险评估列表
     */
    List<TestRiskAssessment> findByExecutionTaskId(Long executionTaskId);
    
    /**
     * 根据风险等级查询风险评估列表
     */
    List<TestRiskAssessment> findByRiskLevel(String riskLevel);
    
    /**
     * 查询指定前缀的评估编码列表（用于编码生成优化）
     */
    List<TestRiskAssessment> findByAssessmentCodeStartingWithOrderByIdDesc(String prefix);
}

