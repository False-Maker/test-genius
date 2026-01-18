package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;

/**
 * 规约注入响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SpecificationInjectionResponseDTO {
    
    /**
     * 增强后的用例内容
     */
    private EnhancedTestCaseDTO enhancedTestCase;
    
    /**
     * 注入的内容列表
     */
    private List<String> injectedContents;
    
    /**
     * 应用的规约列表
     */
    private List<String> appliedSpecs;
}

