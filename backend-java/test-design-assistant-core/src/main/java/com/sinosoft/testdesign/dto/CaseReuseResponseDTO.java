package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.util.Map;

/**
 * 用例复用响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class CaseReuseResponseDTO {
    
    /**
     * 用例ID
     */
    private Long caseId;
    
    /**
     * 用例编码
     */
    private String caseCode;
    
    /**
     * 用例名称
     */
    private String caseName;
    
    /**
     * 相似度分数（用于相似用例搜索）
     */
    private Double similarity;
    
    /**
     * 用例基本信息
     */
    private Map<String, Object> caseInfo;
    
    /**
     * 其他扩展信息
     */
    private Map<String, Object> extraInfo;
}

