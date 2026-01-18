package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字段测试要点响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class FieldTestPointResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 要点编码
     */
    private String pointCode;
    
    /**
     * 关联规约ID
     */
    private Long specId;
    
    /**
     * 字段名称
     */
    private String fieldName;
    
    /**
     * 字段类型：STRING/NUMBER/DATE/BOOLEAN等
     */
    private String fieldType;
    
    /**
     * 测试要求
     */
    private String testRequirement;
    
    /**
     * 测试方法：等价类/边界值/场景法等
     */
    private String testMethod;
    
    /**
     * 测试用例示例（JSON格式）
     */
    private String testCases;
    
    /**
     * 验证规则（JSON格式）
     */
    private String validationRules;
    
    /**
     * 是否必填：1-必填，0-可选
     */
    private String isRequired;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
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
}

