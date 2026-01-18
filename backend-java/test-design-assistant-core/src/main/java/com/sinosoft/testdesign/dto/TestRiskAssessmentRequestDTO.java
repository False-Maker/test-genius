package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 风险评估请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestRiskAssessmentRequestDTO {
    
    /**
     * 评估名称
     */
    @NotBlank(message = "评估名称不能为空")
    @Size(max = 500, message = "评估名称长度不能超过500个字符")
    private String assessmentName;
    
    /**
     * 关联需求ID
     */
    private Long requirementId;
    
    /**
     * 关联执行任务ID
     */
    private Long executionTaskId;
    
    /**
     * 评估人ID
     */
    private Long assessorId;
}

