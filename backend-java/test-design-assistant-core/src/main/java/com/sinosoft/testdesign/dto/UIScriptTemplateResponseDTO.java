package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * UI脚本模板响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class UIScriptTemplateResponseDTO {
    
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
     * 模板类型
     */
    private String templateType;
    
    /**
     * 脚本语言
     */
    private String scriptLanguage;
    
    /**
     * 模板内容
     */
    private String templateContent;
    
    /**
     * 模板变量定义
     */
    private String templateVariables;
    
    /**
     * 适用场景描述
     */
    private String applicableScenarios;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 是否启用
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

