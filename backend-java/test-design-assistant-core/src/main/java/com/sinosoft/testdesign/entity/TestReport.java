package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试报告实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "test_report")
public class TestReport {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 报告编码（RPT-YYYYMMDD-序号）
     */
    @Column(name = "report_code", unique = true, nullable = false, length = 100)
    private String reportCode;
    
    /**
     * 报告名称
     */
    @Column(name = "report_name", nullable = false, length = 500)
    private String reportName;
    
    /**
     * 报告类型：EXECUTION/COVERAGE/QUALITY/RISK
     */
    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;
    
    /**
     * 报告模板ID
     */
    @Column(name = "template_id")
    private Long templateId;
    
    /**
     * 关联需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;
    
    /**
     * 关联执行任务ID
     */
    @Column(name = "execution_task_id")
    private Long executionTaskId;
    
    /**
     * 报告内容（JSON格式）
     */
    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;
    
    /**
     * 报告摘要
     */
    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;
    
    /**
     * 报告状态：DRAFT/PUBLISHED
     */
    @Column(name = "report_status", nullable = false, length = 50)
    private String reportStatus = "DRAFT";
    
    /**
     * 生成配置（JSON格式）
     */
    @Column(name = "generate_config", columnDefinition = "TEXT")
    private String generateConfig;
    
    /**
     * 报告文件URL（Word/PDF）
     */
    @Column(name = "file_url", length = 1000)
    private String fileUrl;
    
    /**
     * 文件格式：WORD/PDF/EXCEL
     */
    @Column(name = "file_format", length = 50)
    private String fileFormat;
    
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
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;
    
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

