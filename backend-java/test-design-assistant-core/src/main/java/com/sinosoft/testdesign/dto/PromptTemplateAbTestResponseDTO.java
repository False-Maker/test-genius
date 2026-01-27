package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 提示词模板A/B测试响应DTO
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
public class PromptTemplateAbTestResponseDTO {
    
    private Long id;
    private Long templateId;
    private String testName;
    private String testDescription;
    private Long versionAId;
    private Long versionBId;
    private Integer trafficSplitA;
    private Integer trafficSplitB;
    private String startTime;
    private String endTime;
    private String status;
    private String autoSelectEnabled;
    private Integer minSamples;
    private String selectionCriteria;
    private Long createdBy;
    private String createdByName;
    private String createTime;
    private String updateTime;
}
