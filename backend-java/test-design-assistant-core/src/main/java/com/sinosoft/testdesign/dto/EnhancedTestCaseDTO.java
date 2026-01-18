package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 增强后的用例DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class EnhancedTestCaseDTO {
    
    /**
     * 用例编码
     */
    private String caseCode;
    
    /**
     * 用例名称
     */
    private String caseName;
    
    /**
     * 前置条件（增强后）
     */
    private String preCondition;
    
    /**
     * 测试步骤（增强后）
     */
    private String testStep;
    
    /**
     * 预期结果
     */
    private String expectedResult;
}

