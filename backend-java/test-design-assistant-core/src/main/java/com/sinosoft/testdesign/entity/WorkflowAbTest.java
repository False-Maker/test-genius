package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流 A/B 测试配置实体（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_ab_test", indexes = {
    @Index(name = "idx_workflow_ab_test_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_workflow_ab_test_status", columnList = "status")
})
public class WorkflowAbTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @Column(name = "test_description", columnDefinition = "TEXT")
    private String testDescription;

    @Column(name = "version_a_id", nullable = false)
    private Long versionAId;

    @Column(name = "version_b_id", nullable = false)
    private Long versionBId;

    @Column(name = "traffic_split_a", nullable = false)
    @Builder.Default
    private Integer trafficSplitA = 50;

    @Column(name = "traffic_split_b", nullable = false)
    @Builder.Default
    private Integer trafficSplitB = 50;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "draft";

    @Column(name = "created_by")
    private Long createdBy;

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
