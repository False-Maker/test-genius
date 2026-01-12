package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试需求实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "test_requirement")
public class TestRequirement {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 需求编码
     */
    @Column(name = "requirement_code", unique = true, nullable = false, length = 100)
    private String requirementCode;
    
    /**
     * 需求名称
     */
    @Column(name = "requirement_name", nullable = false, length = 500)
    private String requirementName;
    
    /**
     * 需求类型：新功能/优化/缺陷修复
     */
    @Column(name = "requirement_type", length = 50)
    private String requirementType;
    
    /**
     * 需求描述
     */
    @Column(name = "requirement_description", columnDefinition = "TEXT")
    private String requirementDescription;
    
    /**
     * 需求文档存储路径
     */
    @Column(name = "requirement_doc_url", length = 1000)
    private String requirementDocUrl;
    
    /**
     * 需求状态：草稿/审核中/已通过/已关闭
     */
    @Column(name = "requirement_status", length = 50)
    private String requirementStatus;
    
    /**
     * 业务模块：投保/核保/理赔/保全等
     */
    @Column(name = "business_module", length = 100)
    private String businessModule;
    
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

