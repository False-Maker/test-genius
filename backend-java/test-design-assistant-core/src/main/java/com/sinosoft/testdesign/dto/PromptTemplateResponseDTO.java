package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class PromptTemplateResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 模板编码
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板分类
     */
    private String templateCategory;
    
    /**
     * 提示词类型
     */
    private String templateType;
    
    /**
     * 模板内容
     */
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    private String templateVariables;
    
    /**
     * 适用的测试分层（逗号分隔）
     */
    private String applicableLayers;
    
    /**
     * 适用的测试方法（逗号分隔）
     */
    private String applicableMethods;
    
    /**
     * 适用的业务模块（逗号分隔）
     */
    private String applicableModules;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

