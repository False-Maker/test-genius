package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * UI脚本模板实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "ui_script_template")
public class UIScriptTemplate {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板编码（TMP-YYYYMMDD-序号）
     */
    @Column(name = "template_code", unique = true, nullable = false, length = 100)
    private String templateCode;
    
    /**
     * 模板名称
     */
    @Column(name = "template_name", nullable = false, length = 500)
    private String templateName;
    
    /**
     * 模板类型：SELENIUM/PLAYWRIGHT/PUPPETEER
     */
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;
    
    /**
     * 脚本语言：PYTHON/JAVA/JAVASCRIPT
     */
    @Column(name = "script_language", nullable = false, length = 50)
    private String scriptLanguage;
    
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
     * 适用场景描述
     */
    @Column(name = "applicable_scenarios", columnDefinition = "TEXT")
    private String applicableScenarios;
    
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

