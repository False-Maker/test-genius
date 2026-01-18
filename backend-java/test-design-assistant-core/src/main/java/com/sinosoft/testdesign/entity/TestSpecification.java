package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 测试规约实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "test_specification")
public class TestSpecification {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 规约编码（SPEC-YYYYMMDD-序号）
     */
    @Column(name = "spec_code", unique = true, nullable = false, length = 100)
    private String specCode;
    
    /**
     * 规约名称
     */
    @Column(name = "spec_name", nullable = false, length = 500)
    private String specName;
    
    /**
     * 规约类型：APPLICATION/PUBLIC（应用级/公共）
     */
    @Column(name = "spec_type", nullable = false, length = 50)
    private String specType;
    
    /**
     * 规约分类
     */
    @Column(name = "spec_category", length = 100)
    private String specCategory;
    
    /**
     * 规约描述
     */
    @Column(name = "spec_description", columnDefinition = "TEXT")
    private String specDescription;
    
    /**
     * 规约内容（JSON格式）
     */
    @Column(name = "spec_content", columnDefinition = "TEXT")
    private String specContent;
    
    /**
     * 适用模块（多个模块用逗号分隔）
     */
    @Column(name = "applicable_modules", length = 500)
    private String applicableModules;
    
    /**
     * 适用测试分层（多个分层用逗号分隔）
     */
    @Column(name = "applicable_layers", length = 500)
    private String applicableLayers;
    
    /**
     * 适用测试方法（多个方法用逗号分隔）
     */
    @Column(name = "applicable_methods", length = 500)
    private String applicableMethods;
    
    /**
     * 当前版本号
     */
    @Column(name = "current_version", length = 50)
    private String currentVersion;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1, columnDefinition = "CHAR(1) DEFAULT '1'")
    private String isActive = "1";
    
    /**
     * 生效日期
     */
    @Column(name = "effective_date")
    private LocalDate effectiveDate;
    
    /**
     * 失效日期
     */
    @Column(name = "expire_date")
    private LocalDate expireDate;
    
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

