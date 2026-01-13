package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试需求响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestRequirementResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 需求编码
     */
    private String requirementCode;
    
    /**
     * 需求名称
     */
    private String requirementName;
    
    /**
     * 需求类型：新功能/优化/缺陷修复
     */
    private String requirementType;
    
    /**
     * 需求描述
     */
    private String requirementDescription;
    
    /**
     * 需求文档存储路径
     */
    private String requirementDocUrl;
    
    /**
     * 需求状态：草稿/审核中/已通过/已关闭
     */
    private String requirementStatus;
    
    /**
     * 业务模块：投保/核保/理赔/保全等
     */
    private String businessModule;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    private String creatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 版本号
     */
    private Integer version;
}

