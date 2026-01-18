package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试执行任务请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestExecutionTaskRequestDTO {
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 500, message = "任务名称长度不能超过500个字符")
    private String taskName;
    
    /**
     * 任务类型：AUTO_SCRIPT_GENERATION/AUTO_SCRIPT_REPAIR/MANUAL_EXECUTION
     */
    @NotBlank(message = "任务类型不能为空")
    @Size(max = 50, message = "任务类型长度不能超过50个字符")
    private String taskType;
    
    /**
     * 关联需求ID
     */
    private Long requirementId;
    
    /**
     * 关联用例ID
     */
    private Long caseId;
    
    /**
     * 关联测试套件ID
     */
    private Long caseSuiteId;
    
    /**
     * 脚本类型：SELENIUM/PLAYWRIGHT/PUPPETEER
     */
    @Size(max = 50, message = "脚本类型长度不能超过50个字符")
    private String scriptType;
    
    /**
     * 脚本内容
     */
    private String scriptContent;
    
    /**
     * 脚本语言：PYTHON/JAVA/JAVASCRIPT
     */
    @Size(max = 50, message = "脚本语言长度不能超过50个字符")
    private String scriptLanguage;
    
    /**
     * 页面代码URL（文件路径或URL）
     */
    @Size(max = 1000, message = "页面代码URL长度不能超过1000个字符")
    private String pageCodeUrl;
    
    /**
     * 自然语言描述（用于脚本生成）
     */
    private String naturalLanguageDesc;
    
    /**
     * 错误日志（用于脚本修复）
     */
    private String errorLog;
    
    /**
     * 执行配置（JSON格式）
     */
    private String executionConfig;
    
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

