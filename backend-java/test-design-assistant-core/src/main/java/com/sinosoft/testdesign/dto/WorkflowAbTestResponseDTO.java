package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流 A/B 测试响应 DTO（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class WorkflowAbTestResponseDTO {

    private Long id;
    private Long workflowId;
    private String testName;
    private String testDescription;
    private Long versionAId;
    private Long versionBId;
    private Integer trafficSplitA;
    private Integer trafficSplitB;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
