package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 规约摘要DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SpecificationSummaryDTO {
    
    /**
     * 规约ID
     */
    private Long id;
    
    /**
     * 规约编码
     */
    private String specCode;
    
    /**
     * 规约名称
     */
    private String specName;
    
    /**
     * 规约类型：APPLICATION/PUBLIC
     */
    private String specType;
    
    /**
     * 当前版本号
     */
    private String currentVersion;
}

