package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UI脚本模板请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class UIScriptTemplateRequestDTO {
    
    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 500, message = "模板名称长度不能超过500个字符")
    private String templateName;
    
    /**
     * 模板类型：SELENIUM/PLAYWRIGHT/PUPPETEER
     */
    @NotBlank(message = "模板类型不能为空")
    @Size(max = 50, message = "模板类型长度不能超过50个字符")
    private String templateType;
    
    /**
     * 脚本语言：PYTHON/JAVA/JAVASCRIPT
     */
    @NotBlank(message = "脚本语言不能为空")
    @Size(max = 50, message = "脚本语言长度不能超过50个字符")
    private String scriptLanguage;
    
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
     * 适用场景描述
     */
    private String applicableScenarios;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Size(max = 1, message = "是否启用长度不能超过1个字符")
    private String isActive;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
}

