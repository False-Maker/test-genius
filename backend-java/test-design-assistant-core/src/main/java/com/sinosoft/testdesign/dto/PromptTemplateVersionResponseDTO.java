package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 提示词模板版本响应DTO
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
public class PromptTemplateVersionResponseDTO {
    
    private Long id;
    private Long templateId;
    private Integer versionNumber;
    private String versionName;
    private String versionDescription;
    private String templateContent;
    private String templateVariables;
    private String changeLog;
    private String isCurrent;
    private Long createdBy;
    private String createdByName;
    private String createTime;
}
