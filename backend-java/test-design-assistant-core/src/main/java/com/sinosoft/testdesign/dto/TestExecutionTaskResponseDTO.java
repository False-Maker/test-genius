package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试执行任务响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestExecutionTaskResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 任务编码
     */
    private String taskCode;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务类型
     */
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
     * 脚本类型
     */
    private String scriptType;
    
    /**
     * 脚本内容
     */
    private String scriptContent;
    
    /**
     * 脚本语言
     */
    private String scriptLanguage;
    
    /**
     * 页面代码URL
     */
    private String pageCodeUrl;
    
    /**
     * 自然语言描述
     */
    private String naturalLanguageDesc;
    
    /**
     * 任务状态
     */
    private String taskStatus;
    
    /**
     * 任务进度（0-100）
     */
    private Integer progress;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    private String creatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}

