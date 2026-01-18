package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 逻辑测试要点实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "logic_test_point")
public class LogicTestPoint {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 要点编码
     */
    @Column(name = "point_code", unique = true, nullable = false, length = 100)
    private String pointCode;
    
    /**
     * 关联规约ID
     */
    @Column(name = "spec_id")
    private Long specId;
    
    /**
     * 逻辑名称
     */
    @Column(name = "logic_name", nullable = false, length = 200)
    private String logicName;
    
    /**
     * 逻辑类型：BUSINESS_RULE/WORKFLOW/BUSINESS_CALCULATION等
     */
    @Column(name = "logic_type", length = 50)
    private String logicType;
    
    /**
     * 逻辑描述
     */
    @Column(name = "logic_description", columnDefinition = "TEXT")
    private String logicDescription;
    
    /**
     * 测试要求
     */
    @Column(name = "test_requirement", columnDefinition = "TEXT")
    private String testRequirement;
    
    /**
     * 测试方法：场景法/决策表/状态转换法等
     */
    @Column(name = "test_method", length = 100)
    private String testMethod;
    
    /**
     * 测试用例示例（JSON格式）
     */
    @Column(name = "test_cases", columnDefinition = "TEXT")
    private String testCases;
    
    /**
     * 验证规则（JSON格式）
     */
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;
    
    /**
     * 适用场景
     */
    @Column(name = "applicable_scenarios", columnDefinition = "TEXT")
    private String applicableScenarios;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1, columnDefinition = "CHAR(1) DEFAULT '1'")
    private String isActive = "1";
    
    /**
     * 显示顺序
     */
    @Column(name = "display_order")
    private Integer displayOrder;
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Column(name = "creator_name", length = 100)
    private String creatorName;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

