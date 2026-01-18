package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试覆盖分析响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestCoverageAnalysisResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分析编码
     */
    private String analysisCode;
    
    /**
     * 分析名称
     */
    private String analysisName;
    
    /**
     * 关联需求ID
     */
    private Long requirementId;
    
    /**
     * 覆盖类型
     */
    private String coverageType;
    
    /**
     * 总项数
     */
    private Integer totalItems;
    
    /**
     * 已覆盖项数
     */
    private Integer coveredItems;
    
    /**
     * 覆盖率（百分比）
     */
    private BigDecimal coverageRate;
    
    /**
     * 未覆盖项列表（JSON格式）
     */
    private String uncoveredItems;
    
    /**
     * 覆盖详情（JSON格式）
     */
    private String coverageDetails;
    
    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;
    
    /**
     * 分析人ID
     */
    private Long analyzerId;
}

