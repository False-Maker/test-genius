package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流版本实体
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_version", indexes = {
    @Index(name = "idx_workflow_version_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_workflow_version_code_version", columnList = "workflow_code, version")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workflow_id", "version"})
})
public class WorkflowVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "workflow_id", nullable = false)
    private Long workflowId; // 工作流定义ID
    
    @Column(name = "workflow_code", length = 100, nullable = false)
    private String workflowCode; // 工作流代码
    
    @Column(name = "version", nullable = false)
    private Integer version; // 版本号
    
    @Column(name = "workflow_config", columnDefinition = "TEXT", nullable = false)
    private String workflowConfig; // 工作流配置（JSON格式）
    
    @Column(name = "version_description", columnDefinition = "TEXT")
    private String versionDescription; // 版本描述
    
    @Column(name = "creator_id")
    private Long creatorId; // 创建人ID
    
    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", insertable = false, updatable = false)
    private WorkflowDefinition workflowDefinition; // 关联的工作流定义
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
