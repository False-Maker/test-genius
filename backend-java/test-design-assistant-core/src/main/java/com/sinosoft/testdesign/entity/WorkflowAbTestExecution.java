package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流 A/B 测试执行记录实体（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_ab_test_execution", indexes = {
    @Index(name = "idx_workflow_ab_test_execution_ab_test_id", columnList = "ab_test_id"),
    @Index(name = "idx_workflow_ab_test_execution_version_id", columnList = "version_id"),
    @Index(name = "idx_workflow_ab_test_execution_request_id", columnList = "request_id"),
    @Index(name = "idx_workflow_ab_test_execution_execution_time", columnList = "execution_time")
})
public class WorkflowAbTestExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ab_test_id", nullable = false)
    private Long abTestId;

    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @Column(name = "version_label", nullable = false, length = 10)
    private String versionLabel;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "success";

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "execution_time")
    private LocalDateTime executionTime;

    @PrePersist
    public void prePersist() {
        if (executionTime == null) executionTime = LocalDateTime.now();
    }
}
