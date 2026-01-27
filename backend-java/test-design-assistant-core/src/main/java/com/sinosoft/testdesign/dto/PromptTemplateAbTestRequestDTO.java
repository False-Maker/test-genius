package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提示词模板A/B测试请求DTO
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
public class PromptTemplateAbTestRequestDTO {
    
    @NotNull(message = "测试名称不能为空")
    @Size(max = 200, message = "测试名称长度不能超过200个字符")
    private String testName;
    
    private String testDescription;
    
    @NotNull(message = "版本A ID不能为空")
    private Long versionAId;
    
    @NotNull(message = "版本B ID不能为空")
    private Long versionBId;
    
    private Integer trafficSplitA = 50;
    
    private Integer trafficSplitB = 50;
    
    private String autoSelectEnabled = "0";
    
    private Integer minSamples = 100;
    
    private String selectionCriteria = "success_rate";
}
