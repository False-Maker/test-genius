package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 逻辑测试要点请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class LogicTestPointRequestDTO {
    
    /**
     * 关联规约ID
     */
    private Long specId;
    
    /**
     * 逻辑名称
     */
    @NotBlank(message = "逻辑名称不能为空")
    @Size(max = 200, message = "逻辑名称长度不能超过200个字符")
    private String logicName;
    
    /**
     * 逻辑类型：BUSINESS_RULE/WORKFLOW/BUSINESS_CALCULATION等
     */
    @Size(max = 50, message = "逻辑类型长度不能超过50个字符")
    private String logicType;
    
    /**
     * 逻辑描述
     */
    private String logicDescription;
    
    /**
     * 测试要求
     */
    private String testRequirement;
    
    /**
     * 测试方法：场景法/决策表/状态转换法等
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
     * 适用场景
     */
    private String applicableScenarios;
    
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

