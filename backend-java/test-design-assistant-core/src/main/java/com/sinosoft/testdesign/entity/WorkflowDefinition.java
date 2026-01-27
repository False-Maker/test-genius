package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流定义实体
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_definition", indexes = {
    @Index(name = "idx_workflow_definition_code", columnList = "workflow_code"),
    @Index(name = "idx_workflow_definition_type", columnList = "workflow_type"),
    @Index(name = "idx_workflow_definition_is_active", columnList = "is_active")
})
public class WorkflowDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "workflow_code", length = 100, unique = true, nullable = false)
    private String workflowCode; // 工作流代码
    
    @Column(name = "workflow_name", length = 200, nullable = false)
    private String workflowName; // 工作流名称
    
    @Column(name = "workflow_description", columnDefinition = "TEXT")
    private String workflowDescription; // 工作流描述
    
    @Column(name = "workflow_type", length = 50)
    private String workflowType; // 工作流类型
    
    @Column(name = "workflow_config", columnDefinition = "TEXT", nullable = false)
    private String workflowConfig; // 工作流配置（JSON格式）
    
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1; // 版本号
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 是否启用
    
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false; // 是否为默认工作流
    
    @Column(name = "creator_id")
    private Long creatorId; // 创建人ID
    
    @Column(name = "creator_name", length = 100)
    private String creatorName; // 创建人姓名
    
    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间
    
    @Column(name = "update_time")
    private LocalDateTime updateTime; // 更新时间
    
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime; // 最后执行时间
    
    @Column(name = "execution_count", nullable = false)
    @Builder.Default
    private Integer executionCount = 0; // 执行次数
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (executionCount == null) {
            executionCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
