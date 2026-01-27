package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工作流定义请求DTO
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
public class WorkflowDefinitionRequestDTO {
    
    /**
     * 工作流代码（唯一标识）
     */
    @NotBlank(message = "工作流代码不能为空")
    @Size(max = 100, message = "工作流代码长度不能超过100个字符")
    private String workflowCode;
    
    /**
     * 工作流名称
     */
    @NotBlank(message = "工作流名称不能为空")
    @Size(max = 200, message = "工作流名称长度不能超过200个字符")
    private String workflowName;
    
    /**
     * 工作流类型：CASE_GENERATION/UI_SCRIPT_GENERATION等
     */
    @Size(max = 50, message = "工作流类型长度不能超过50个字符")
    private String workflowType;
    
    /**
     * 工作流描述
     */
    private String description;
    
    /**
     * 工作流配置（JSON格式，包含节点和连线信息）
     */
    @NotBlank(message = "工作流配置不能为空")
    private String workflowConfig;
    
    /**
     * 是否启用
     */
    private Boolean isActive;
    
    /**
     * 是否为默认工作流
     */
    private Boolean isDefault;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String creatorName;
}
