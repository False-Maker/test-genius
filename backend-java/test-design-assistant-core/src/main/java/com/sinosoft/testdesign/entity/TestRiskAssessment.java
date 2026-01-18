package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险评估实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "test_risk_assessment")
public class TestRiskAssessment {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 评估编码（RISK-YYYYMMDD-序号）
     */
    @Column(name = "assessment_code", unique = true, nullable = false, length = 100)
    private String assessmentCode;
    
    /**
     * 评估名称
     */
    @Column(name = "assessment_name", nullable = false, length = 500)
    private String assessmentName;
    
    /**
     * 关联需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;
    
    /**
     * 关联执行任务ID
     */
    @Column(name = "execution_task_id")
    private Long executionTaskId;
    
    /**
     * 风险等级：HIGH/MEDIUM/LOW
     */
    @Column(name = "risk_level", length = 50)
    private String riskLevel;
    
    /**
     * 风险评分（0-100）
     */
    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;
    
    /**
     * 风险项列表（JSON格式）
     */
    @Column(name = "risk_items", columnDefinition = "TEXT")
    private String riskItems;
    
    /**
     * 上线可行性评分（0-100）
     */
    @Column(name = "feasibility_score", precision = 5, scale = 2)
    private BigDecimal feasibilityScore;
    
    /**
     * 上线建议
     */
    @Column(name = "feasibility_recommendation", columnDefinition = "TEXT")
    private String feasibilityRecommendation;
    
    /**
     * 评估详情（JSON格式）
     */
    @Column(name = "assessment_details", columnDefinition = "TEXT")
    private String assessmentDetails;
    
    /**
     * 评估时间
     */
    @Column(name = "assessment_time")
    private LocalDateTime assessmentTime;
    
    /**
     * 评估人ID
     */
    @Column(name = "assessor_id")
    private Long assessorId;
    
    @PrePersist
    public void prePersist() {
        if (this.assessmentTime == null) {
            this.assessmentTime = LocalDateTime.now();
        }
    }
}

