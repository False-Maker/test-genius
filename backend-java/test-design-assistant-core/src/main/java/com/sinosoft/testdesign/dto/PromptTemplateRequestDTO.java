package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提示词模板请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class PromptTemplateRequestDTO {
    
    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 500, message = "模板名称长度不能超过500个字符")
    private String templateName;
    
    /**
     * 模板分类
     */
    @Size(max = 100, message = "模板分类长度不能超过100个字符")
    private String templateCategory;
    
    /**
     * 提示词类型
     */
    @Size(max = 50, message = "提示词类型长度不能超过50个字符")
    private String templateType;
    
    /**
     * 模板内容
     */
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    private String templateVariables;
    
    /**
     * 适用的测试分层（逗号分隔）
     */
    @Size(max = 500, message = "适用的测试分层长度不能超过500个字符")
    private String applicableLayers;
    
    /**
     * 适用的测试方法（逗号分隔）
     */
    @Size(max = 500, message = "适用的测试方法长度不能超过500个字符")
    private String applicableMethods;
    
    /**
     * 适用的业务模块（逗号分隔）
     */
    @Size(max = 500, message = "适用的业务模块长度不能超过500个字符")
    private String applicableModules;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Size(max = 1, message = "启用状态长度不能超过1个字符")
    private String isActive;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
}

