package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工作流 A/B 测试请求 DTO（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class WorkflowAbTestRequestDTO {

    @NotNull(message = "测试名称不能为空")
    @Size(max = 200)
    private String testName;

    private String testDescription;

    @NotNull(message = "版本 A ID 不能为空")
    private Long versionAId;

    @NotNull(message = "版本 B ID 不能为空")
    private Long versionBId;

    private Integer trafficSplitA = 50;
    private Integer trafficSplitB = 50;
    private Long createdBy;
}
