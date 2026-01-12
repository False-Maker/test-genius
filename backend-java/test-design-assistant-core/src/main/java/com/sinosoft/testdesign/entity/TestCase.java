package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试用例实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "test_case")
public class TestCase {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用例编码
     */
    @Column(name = "case_code", unique = true, nullable = false, length = 100)
    private String caseCode;
    
    /**
     * 用例名称
     */
    @Column(name = "case_name", nullable = false, length = 500)
    private String caseName;
    
    /**
     * 需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;
    
    /**
     * 测试分层ID
     */
    @Column(name = "layer_id")
    private Long layerId;
    
    /**
     * 测试方法ID
     */
    @Column(name = "method_id")
    private Long methodId;
    
    /**
     * 用例类型：正常/异常/边界
     */
    @Column(name = "case_type", length = 50)
    private String caseType;
    
    /**
     * 用例优先级：高/中/低
     */
    @Column(name = "case_priority", length = 50)
    private String casePriority;
    
    /**
     * 前置条件
     */
    @Column(name = "pre_condition", columnDefinition = "TEXT")
    private String preCondition;
    
    /**
     * 测试步骤
     */
    @Column(name = "test_step", columnDefinition = "TEXT")
    private String testStep;
    
    /**
     * 预期结果
     */
    @Column(name = "expected_result", columnDefinition = "TEXT")
    private String expectedResult;
    
    /**
     * 用例状态：草稿/待审核/已审核/已废弃
     */
    @Column(name = "case_status", length = 50)
    private String caseStatus;
    
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
    
    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version = 1;
    
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

