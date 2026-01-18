package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestRiskAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风险评估服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface TestRiskAssessmentService {
    
    /**
     * 执行风险评估
     * @param assessment 评估信息
     * @return 生成的风险评估
     */
    TestRiskAssessment assessRisk(TestRiskAssessment assessment);
    
    /**
     * 评估需求风险
     * @param requirementId 需求ID
     * @return 生成的风险评估
     */
    TestRiskAssessment assessRequirementRisk(Long requirementId);
    
    /**
     * 评估执行任务风险
     * @param executionTaskId 执行任务ID
     * @return 生成的风险评估
     */
    TestRiskAssessment assessExecutionTaskRisk(Long executionTaskId);
    
    /**
     * 评估风险等级
     * @param riskScore 风险评分（0-100）
     * @return 风险等级（HIGH/MEDIUM/LOW）
     */
    String assessRiskLevel(BigDecimal riskScore);
    
    /**
     * 评估上线可行性
     * @param requirementId 需求ID（可选）
     * @param executionTaskId 执行任务ID（可选）
     * @return 可行性评分（0-100）
     */
    BigDecimal assessFeasibility(Long requirementId, Long executionTaskId);
    
    /**
     * 识别风险项
     * @param requirementId 需求ID（可选）
     * @param executionTaskId 执行任务ID（可选）
     * @return 风险项列表（JSON格式）
     */
    String identifyRiskItems(Long requirementId, Long executionTaskId);
    
    /**
     * 根据ID查询风险评估
     * @param id 评估ID
     * @return 风险评估详情
     */
    TestRiskAssessment getAssessmentById(Long id);
    
    /**
     * 根据编码查询风险评估
     * @param assessmentCode 评估编码
     * @return 风险评估详情
     */
    TestRiskAssessment getAssessmentByCode(String assessmentCode);
    
    /**
     * 分页查询风险评估列表
     * @param pageable 分页参数
     * @return 风险评估列表
     */
    Page<TestRiskAssessment> getAssessmentList(Pageable pageable);
    
    /**
     * 根据需求ID查询风险评估列表
     * @param requirementId 需求ID
     * @return 风险评估列表
     */
    List<TestRiskAssessment> getAssessmentByRequirementId(Long requirementId);
    
    /**
     * 根据执行任务ID查询风险评估列表
     * @param executionTaskId 执行任务ID
     * @return 风险评估列表
     */
    List<TestRiskAssessment> getAssessmentByExecutionTaskId(Long executionTaskId);
    
    /**
     * 根据风险等级查询风险评估列表
     * @param riskLevel 风险等级
     * @return 风险评估列表
     */
    List<TestRiskAssessment> getAssessmentByRiskLevel(String riskLevel);
}

