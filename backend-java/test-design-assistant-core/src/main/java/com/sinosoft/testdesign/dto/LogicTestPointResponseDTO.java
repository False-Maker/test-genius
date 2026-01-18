package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 逻辑测试要点响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class LogicTestPointResponseDTO {
    
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
     * 逻辑名称
     */
    private String logicName;
    
    /**
     * 逻辑类型：BUSINESS_RULE/WORKFLOW/BUSINESS_CALCULATION等
     */
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

