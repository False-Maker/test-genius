package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试覆盖分析实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "test_coverage_analysis")
public class TestCoverageAnalysis {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分析编码（COV-YYYYMMDD-序号）
     */
    @Column(name = "analysis_code", unique = true, nullable = false, length = 100)
    private String analysisCode;
    
    /**
     * 分析名称
     */
    @Column(name = "analysis_name", nullable = false, length = 500)
    private String analysisName;
    
    /**
     * 关联需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;
    
    /**
     * 覆盖类型：REQUIREMENT/FUNCTION/SCENARIO/CODE
     */
    @Column(name = "coverage_type", nullable = false, length = 50)
    private String coverageType;
    
    /**
     * 总项数
     */
    @Column(name = "total_items")
    private Integer totalItems;
    
    /**
     * 已覆盖项数
     */
    @Column(name = "covered_items")
    private Integer coveredItems;
    
    /**
     * 覆盖率（百分比）
     */
    @Column(name = "coverage_rate", precision = 5, scale = 2)
    private BigDecimal coverageRate;
    
    /**
     * 未覆盖项列表（JSON格式）
     */
    @Column(name = "uncovered_items", columnDefinition = "TEXT")
    private String uncoveredItems;
    
    /**
     * 覆盖详情（JSON格式）
     */
    @Column(name = "coverage_details", columnDefinition = "TEXT")
    private String coverageDetails;
    
    /**
     * 分析时间
     */
    @Column(name = "analysis_time")
    private LocalDateTime analysisTime;
    
    /**
     * 分析人ID
     */
    @Column(name = "analyzer_id")
    private Long analyzerId;
    
    @PrePersist
    public void prePersist() {
        if (this.analysisTime == null) {
            this.analysisTime = LocalDateTime.now();
        }
    }
}

