package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板版本管理实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "prompt_template_version", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"template_id", "version_number"})
})
public class PromptTemplateVersion {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板ID
     */
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    /**
     * 版本号
     */
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    
    /**
     * 版本名称
     */
    @Column(name = "version_name", length = 200)
    private String versionName;
    
    /**
     * 版本描述
     */
    @Column(name = "version_description", columnDefinition = "TEXT")
    private String versionDescription;
    
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
     * 变更日志
     */
    @Column(name = "change_log", columnDefinition = "TEXT")
    private String changeLog;
    
    /**
     * 是否当前版本：1-是，0-否
     */
    @Column(name = "is_current", length = 1, columnDefinition = "CHAR(1) DEFAULT '0'")
    private String isCurrent = "0";
    
    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    @Column(name = "created_by_name", length = 100)
    private String createdByName;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.isCurrent == null) {
            this.isCurrent = "0";
        }
    }
}
