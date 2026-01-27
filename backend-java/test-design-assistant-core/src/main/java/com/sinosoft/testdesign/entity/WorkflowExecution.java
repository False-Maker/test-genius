package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流执行记录实体
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_execution", indexes = {
    @Index(name = "idx_workflow_execution_execution_id", columnList = "execution_id"),
    @Index(name = "idx_workflow_execution_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_workflow_execution_workflow_code", columnList = "workflow_code"),
    @Index(name = "idx_workflow_execution_status", columnList = "status"),
    @Index(name = "idx_workflow_execution_create_time", columnList = "create_time")
})
public class WorkflowExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "execution_id", length = 100, unique = true, nullable = false)
    private String executionId; // 执行ID
    
    @Column(name = "workflow_id", nullable = false)
    private Long workflowId; // 工作流定义ID
    
    @Column(name = "workflow_code", length = 100, nullable = false)
    private String workflowCode; // 工作流代码
    
    @Column(name = "workflow_version", nullable = false)
    private Integer workflowVersion; // 工作流版本
    
    @Column(name = "execution_type", length = 50, nullable = false)
    private String executionType; // 执行类型：MANUAL/SCHEDULED/API
    
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData; // 输入数据（JSON格式）
    
    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData; // 输出数据（JSON格式）
    
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // 状态：PENDING/RUNNING/SUCCESS/FAILED/CANCELLED
    
    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Integer progress = 0; // 进度（0-100）
    
    @Column(name = "current_node_id", length = 100)
    private String currentNodeId; // 当前执行节点ID
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 错误信息
    
    @Column(name = "error_node_id", length = 100)
    private String errorNodeId; // 错误节点ID
    
    @Column(name = "execution_log", columnDefinition = "TEXT")
    private String executionLog; // 执行日志（JSON格式）
    
    @Column(name = "start_time")
    private LocalDateTime startTime; // 开始时间
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // 结束时间
    
    @Column(name = "duration")
    private Integer duration; // 执行耗时（毫秒）
    
    @Column(name = "creator_id")
    private Long creatorId; // 创建人ID
    
    @Column(name = "creator_name", length = 100)
    private String creatorName; // 创建人姓名
    
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
        if (status == null) {
            status = "PENDING";
        }
        if (progress == null) {
            progress = 0;
        }
    }
}
