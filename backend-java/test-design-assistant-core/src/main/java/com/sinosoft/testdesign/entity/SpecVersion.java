package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 规约版本管理实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "spec_version", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"spec_id", "version_number"})
})
public class SpecVersion {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 规约ID
     */
    @Column(name = "spec_id", nullable = false)
    private Long specId;
    
    /**
     * 版本号
     */
    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;
    
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
     * 规约内容（JSON格式）
     */
    @Column(name = "spec_content", columnDefinition = "TEXT")
    private String specContent;
    
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
        this.createTime = LocalDateTime.now();
    }
}

