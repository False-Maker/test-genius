package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;

/**
 * 规约检查响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SpecificationCheckResponseDTO {
    
    /**
     * 是否符合规约
     */
    private Boolean isCompliant;
    
    /**
     * 符合度评分（0-100）
     */
    private Double complianceScore;
    
    /**
     * 总检查项数
     */
    private Integer totalChecks;
    
    /**
     * 通过的检查项数
     */
    private Integer passedChecks;
    
    /**
     * 未通过的检查项数
     */
    private Integer failedChecks;
    
    /**
     * 不符合项列表
     */
    private List<ComplianceIssueDTO> issues;
    
    /**
     * 匹配的规约列表
     */
    private List<SpecificationSummaryDTO> matchedSpecifications;
}

