package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试执行记录响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestExecutionRecordResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 执行记录编码
     */
    private String recordCode;
    
    /**
     * 执行任务ID
     */
    private Long taskId;
    
    /**
     * 关联用例ID
     */
    private Long caseId;
    
    /**
     * 执行类型
     */
    private String executionType;
    
    /**
     * 执行状态
     */
    private String executionStatus;
    
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
    private String executedByName;
    
    /**
     * 执行时间
     */
    private LocalDateTime executionTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    
    /**
     * 截图URL
     */
    private String screenshotUrl;
    
    /**
     * 视频URL
     */
    private String videoUrl;
}

