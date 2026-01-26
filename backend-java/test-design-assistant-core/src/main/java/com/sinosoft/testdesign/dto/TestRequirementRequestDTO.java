package com.sinosoft.testdesign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试需求请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRequirementRequestDTO {
    
    /**
     * 需求名称
     */
    @NotBlank(message = "需求名称不能为空")
    @Size(max = 500, message = "需求名称长度不能超过500个字符")
    private String requirementName;
    
    /**
     * 需求类型：新功能/优化/缺陷修复
     */
    @Size(max = 50, message = "需求类型长度不能超过50个字符")
    private String requirementType;
    
    /**
     * 需求描述
     */
    private String requirementDescription;
    
    /**
     * 需求文档存储路径
     */
    @Size(max = 1000, message = "需求文档路径长度不能超过1000个字符")
    private String requirementDocUrl;
    
    /**
     * 需求状态：草稿/审核中/已通过/已关闭
     */
    @Size(max = 50, message = "需求状态长度不能超过50个字符")
    private String requirementStatus;
    
    /**
     * 业务模块：投保/核保/理赔/保全等
     */
    @Size(max = 100, message = "业务模块长度不能超过100个字符")
    private String businessModule;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String creatorName;
}

