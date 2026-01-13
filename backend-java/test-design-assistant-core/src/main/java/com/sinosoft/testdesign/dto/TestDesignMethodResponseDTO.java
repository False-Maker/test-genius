package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 测试设计方法响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestDesignMethodResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 方法编码
     */
    private String methodCode;
    
    /**
     * 方法名称
     */
    private String methodName;
    
    /**
     * 方法描述
     */
    private String methodDescription;
    
    /**
     * 适用的测试分层（逗号分隔）
     */
    private String applicableLayers;
    
    /**
     * 使用示例
     */
    private String example;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
}

