package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 符合性检查问题DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class ComplianceIssueDTO {
    
    /**
     * 规约编码
     */
    private String specCode;
    
    /**
     * 规约名称
     */
    private String specName;
    
    /**
     * 问题类型：FIELD_REQUIREMENT/LOGIC_REQUIREMENT/FORMAT_REQUIREMENT等
     */
    private String issueType;
    
    /**
     * 问题描述
     */
    private String issueDescription;
    
    /**
     * 严重程度：HIGH/MEDIUM/LOW
     */
    private String severity;
    
    /**
     * 改进建议
     */
    private String suggestion;
}

