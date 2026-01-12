package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 用例生成请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class CaseGenerationRequest {
    
    /**
     * 需求ID
     */
    private Long requirementId;
    
    /**
     * 测试分层编码
     */
    private String layerCode;
    
    /**
     * 测试方法编码
     */
    private String methodCode;
    
    /**
     * 提示词模板ID（可选）
     */
    private Long templateId;
    
    /**
     * 模型编码（可选，如果不提供则使用默认模型）
     */
    private String modelCode;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
}

