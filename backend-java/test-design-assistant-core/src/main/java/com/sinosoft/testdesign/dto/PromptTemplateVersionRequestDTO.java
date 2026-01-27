package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提示词模板版本请求DTO
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
public class PromptTemplateVersionRequestDTO {
    
    /**
     * 版本号（可选，如果不提供则自动生成）
     */
    private Integer versionNumber;
    
    /**
     * 版本名称
     */
    @Size(max = 200, message = "版本名称长度不能超过200个字符")
    private String versionName;
    
    /**
     * 版本描述
     */
    private String versionDescription;
    
    /**
     * 模板内容
     */
    @NotNull(message = "模板内容不能为空")
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    private String templateVariables;
    
    /**
     * 变更日志
     */
    private String changeLog;
    
    /**
     * 是否设置为当前版本：1-是，0-否
     */
    private String isCurrent;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String createdByName;
}
