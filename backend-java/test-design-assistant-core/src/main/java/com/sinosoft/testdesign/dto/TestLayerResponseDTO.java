package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 测试分层响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestLayerResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分层编码
     */
    private String layerCode;
    
    /**
     * 分层名称
     */
    private String layerName;
    
    /**
     * 分层描述
     */
    private String layerDescription;
    
    /**
     * 排序顺序
     */
    private Integer layerOrder;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
}

