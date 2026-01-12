package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用例生成任务DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class GenerationTaskDTO {
    
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 任务编码
     */
    private String taskCode;
    
    /**
     * 需求ID
     */
    private Long requirementId;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 进度（0-100）
     */
    private Integer progress;
    
    /**
     * 总用例数
     */
    private Integer totalCases;
    
    /**
     * 成功用例数
     */
    private Integer successCases;
    
    /**
     * 失败用例数
     */
    private Integer failCases;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 结果数据
     */
    private Object result;
    
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
    private LocalDateTime completeTime;
}

