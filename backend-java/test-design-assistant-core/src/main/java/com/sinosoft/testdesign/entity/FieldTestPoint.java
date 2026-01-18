package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段测试要点实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "field_test_point")
public class FieldTestPoint {
    
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
     * 字段名称
     */
    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;
    
    /**
     * 字段类型：STRING/NUMBER/DATE/BOOLEAN等
     */
    @Column(name = "field_type", length = 50)
    private String fieldType;
    
    /**
     * 测试要求
     */
    @Column(name = "test_requirement", columnDefinition = "TEXT")
    private String testRequirement;
    
    /**
     * 测试方法：等价类/边界值/场景法等
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
     * 是否必填：1-必填，0-可选
     */
    @Column(name = "is_required", length = 1)
    private String isRequired = "0";
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1)
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

