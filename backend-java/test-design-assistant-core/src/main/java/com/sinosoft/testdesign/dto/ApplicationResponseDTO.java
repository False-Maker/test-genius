package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用管理响应 DTO（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class ApplicationResponseDTO {

    private Long id;
    private String appCode;
    private String appName;
    private String appType;
    private Long workflowId;
    private Long promptTemplateId;
    private String description;
    private Boolean isActive;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
