package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险评估响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestRiskAssessmentResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 评估编码
     */
    private String assessmentCode;
    
    /**
     * 评估名称
     */
    private String assessmentName;
    
    /**
     * 关联需求ID
     */
    private Long requirementId;
    
    /**
     * 关联执行任务ID
     */
    private Long executionTaskId;
    
    /**
     * 风险等级：HIGH/MEDIUM/LOW
     */
    private String riskLevel;
    
    /**
     * 风险评分（0-100）
     */
    private BigDecimal riskScore;
    
    /**
     * 风险项列表（JSON格式）
     */
    private String riskItems;
    
    /**
     * 上线可行性评分（0-100）
     */
    private BigDecimal feasibilityScore;
    
    /**
     * 上线建议
     */
    private String feasibilityRecommendation;
    
    /**
     * 评估详情（JSON格式）
     */
    private String assessmentDetails;
    
    /**
     * 评估时间
     */
    private LocalDateTime assessmentTime;
    
    /**
     * 评估人ID
     */
    private Long assessorId;
}

