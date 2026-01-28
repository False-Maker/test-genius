package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 应用管理请求 DTO（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class ApplicationRequestDTO {

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 200)
    private String appName;

    @Size(max = 100)
    private String appCode;

    @NotBlank(message = "应用类型不能为空")
    @Size(max = 50)
    private String appType;

    private Long workflowId;
    private Long promptTemplateId;
    private String description;
    private Boolean isActive = true;
    private Long creatorId;
}
