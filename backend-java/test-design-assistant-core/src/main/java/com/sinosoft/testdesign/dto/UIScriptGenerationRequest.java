package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * UI脚本生成请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class UIScriptGenerationRequest {
    
    /**
     * 自然语言描述
     */
    private String naturalLanguageDesc;
    
    /**
     * 页面代码URL或文件路径（可选）
     */
    private String pageCodeUrl;
    
    /**
     * 页面元素信息列表（可选，如果提供则跳过解析）
     */
    private List<Map<String, Object>> pageElements;
    
    /**
     * 脚本类型：SELENIUM/PLAYWRIGHT
     */
    private String scriptType = "SELENIUM";
    
    /**
     * 脚本语言：PYTHON/JAVA/JAVASCRIPT
     */
    private String scriptLanguage = "PYTHON";
    
    /**
     * 页面URL（用于脚本中的页面打开）
     */
    private String pageUrl;
    
    /**
     * 是否使用大语言模型优化脚本生成
     */
    private Boolean useLlm = true;
    
    /**
     * 关联需求ID（可选）
     */
    private Long requirementId;
    
    /**
     * 关联用例ID（可选）
     */
    private Long caseId;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    private String creatorName;
}

