package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试报告模板实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "test_report_template")
public class TestReportTemplate {
    
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
     * 模板类型：EXECUTION/COVERAGE/QUALITY/RISK
     */
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;
    
    /**
     * 模板内容（JSON格式）
     */
    @Column(name = "template_content", nullable = false, columnDefinition = "TEXT")
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables;
    
    /**
     * 文件格式：WORD/PDF/EXCEL
     */
    @Column(name = "file_format", length = 50)
    private String fileFormat;
    
    /**
     * 模板描述
     */
    @Column(name = "template_description", columnDefinition = "TEXT")
    private String templateDescription;
    
    /**
     * 是否默认模板：1-是，0-否
     */
    @Column(name = "is_default", length = 1)
    private String isDefault = "0";
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
    
    /**
     * 版本号
     */
    @Column(name = "version")
    private Integer version = 1;
    
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

