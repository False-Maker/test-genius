package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 用例生成结果DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class CaseGenerationResult {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务状态：PENDING/PROCESSING/SUCCESS/FAILED
     */
    private String status;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 生成的用例数量
     */
    private Integer caseCount;
}

