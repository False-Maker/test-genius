package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流定义响应DTO
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
public class WorkflowDefinitionResponseDTO {
    
    private Long id;
    private String workflowCode;
    private String workflowName;
    private String workflowType;
    private Integer workflowVersion;
    private String description;
    private String workflowConfig;
    private Boolean isActive;
    private Boolean isDefault;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer version;
}
