package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 应用管理实体（第四阶段 4.3）
 * 逻辑应用，关联工作流/提示词；版本管理复用 workflow_version / prompt_template_version
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "application", indexes = {
    @Index(name = "idx_application_app_code", columnList = "app_code"),
    @Index(name = "idx_application_app_type", columnList = "app_type"),
    @Index(name = "idx_application_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_application_prompt_template_id", columnList = "prompt_template_id")
})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_code", unique = true, nullable = false, length = 100)
    private String appCode;

    @Column(name = "app_name", nullable = false, length = 200)
    private String appName;

    @Column(name = "app_type", nullable = false, length = 50)
    private String appType;

    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "prompt_template_id")
    private Long promptTemplateId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) createTime = LocalDateTime.now();
        if (updateTime == null) updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}
