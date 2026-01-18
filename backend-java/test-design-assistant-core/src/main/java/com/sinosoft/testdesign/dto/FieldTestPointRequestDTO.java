package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字段测试要点请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class FieldTestPointRequestDTO {
    
    /**
     * 关联规约ID
     */
    private Long specId;
    
    /**
     * 字段名称
     */
    @NotBlank(message = "字段名称不能为空")
    @Size(max = 200, message = "字段名称长度不能超过200个字符")
    private String fieldName;
    
    /**
     * 字段类型：STRING/NUMBER/DATE/BOOLEAN等
     */
    @Size(max = 50, message = "字段类型长度不能超过50个字符")
    private String fieldType;
    
    /**
     * 测试要求
     */
    private String testRequirement;
    
    /**
     * 测试方法：等价类/边界值/场景法等
     */
    @Size(max = 100, message = "测试方法长度不能超过100个字符")
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
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String creatorName;
}

