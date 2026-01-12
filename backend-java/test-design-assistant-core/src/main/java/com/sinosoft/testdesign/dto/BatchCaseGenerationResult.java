package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量用例生成结果DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class BatchCaseGenerationResult {
    
    /**
     * 批量任务ID（父任务ID）
     */
    private Long batchTaskId;
    
    /**
     * 批量任务编码
     */
    private String batchTaskCode;
    
    /**
     * 总任务数
     */
    private Integer totalTasks;
    
    /**
     * 成功创建的任务数
     */
    private Integer successTasks;
    
    /**
     * 失败的任务数
     */
    private Integer failTasks;
    
    /**
     * 子任务列表（任务ID列表）
     */
    private List<Long> taskIds;
    
    /**
     * 状态：PROCESSING/SUCCESS/FAILED
     */
    private String status;
    
    /**
     * 消息
     */
    private String message;
}

