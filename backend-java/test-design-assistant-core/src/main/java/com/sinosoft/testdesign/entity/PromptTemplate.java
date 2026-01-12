package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "prompt_template")
public class PromptTemplate {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板编码
     */
    @Column(name = "template_code", unique = true, nullable = false, length = 100)
    private String templateCode;
    
    /**
     * 模板名称
     */
    @Column(name = "template_name", nullable = false, length = 500)
    private String templateName;
    
    /**
     * 模板分类
     */
    @Column(name = "template_category", length = 100)
    private String templateCategory;
    
    /**
     * 提示词类型
     */
    @Column(name = "template_type", length = 50)
    private String templateType;
    
    /**
     * 模板内容
     */
    @Column(name = "template_content", nullable = false, columnDefinition = "TEXT")
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables;
    
    /**
     * 适用的测试分层（逗号分隔）
     */
    @Column(name = "applicable_layers", length = 500)
    private String applicableLayers;
    
    /**
     * 适用的测试方法（逗号分隔）
     */
    @Column(name = "applicable_methods", length = 500)
    private String applicableMethods;
    
    /**
     * 适用的业务模块（逗号分隔）
     */
    @Column(name = "applicable_modules", length = 500)
    private String applicableModules;
    
    /**
     * 模板描述
     */
    @Column(name = "template_description", columnDefinition = "TEXT")
    private String templateDescription;
    
    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version = 1;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
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

