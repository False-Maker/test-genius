package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * UI脚本生成结果DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class UIScriptGenerationResult {
    
    /**
     * 任务编码
     */
    private String taskCode;
    
    /**
     * 任务状态：PENDING/PROCESSING/SUCCESS/FAILED
     */
    private String taskStatus;
    
    /**
     * 任务进度（0-100）
     */
    private Integer progress;
    
    /**
     * 生成的脚本内容
     */
    private String scriptContent;
    
    /**
     * 脚本类型
     */
    private String scriptType;
    
    /**
     * 脚本语言
     */
    private String scriptLanguage;
    
    /**
     * 使用的元素列表
     */
    private List<Map<String, Object>> elementsUsed;
    
    /**
     * 操作步骤列表
     */
    private List<Map<String, Object>> steps;
    
    /**
     * 页面URL
     */
    private String pageUrl;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 完成时间
     */
    private String finishTime;
}

