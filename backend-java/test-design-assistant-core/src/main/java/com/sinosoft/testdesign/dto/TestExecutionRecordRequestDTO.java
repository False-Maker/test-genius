package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试执行记录请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestExecutionRecordRequestDTO {
    
    /**
     * 执行任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    /**
     * 关联用例ID
     */
    private Long caseId;
    
    /**
     * 执行类型：MANUAL/AUTOMATED
     */
    @Size(max = 50, message = "执行类型长度不能超过50个字符")
    private String executionType;
    
    /**
     * 执行结果详情
     */
    private String executionResult;
    
    /**
     * 执行日志
     */
    private String executionLog;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行耗时（毫秒）
     */
    private Integer executionDuration;
    
    /**
     * 执行人ID
     */
    private Long executedBy;
    
    /**
     * 执行人姓名
     */
    @Size(max = 100, message = "执行人姓名长度不能超过100个字符")
    private String executedByName;
    
    /**
     * 截图URL（失败时）
     */
    @Size(max = 1000, message = "截图URL长度不能超过1000个字符")
    private String screenshotUrl;
    
    /**
     * 视频URL（可选）
     */
    @Size(max = 1000, message = "视频URL长度不能超过1000个字符")
    private String videoUrl;
}

