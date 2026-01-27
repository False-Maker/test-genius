package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流节点执行记录实体
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workflow_node_execution", indexes = {
    @Index(name = "idx_workflow_node_execution_execution_id", columnList = "execution_id"),
    @Index(name = "idx_workflow_node_execution_node_id", columnList = "node_id"),
    @Index(name = "idx_workflow_node_execution_status", columnList = "status")
})
public class WorkflowNodeExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "execution_id", length = 100, nullable = false)
    private String executionId; // 工作流执行ID
    
    @Column(name = "node_id", length = 100, nullable = false)
    private String nodeId; // 节点ID
    
    @Column(name = "node_type", length = 50, nullable = false)
    private String nodeType; // 节点类型：input/process/transform/output/condition/loop
    
    @Column(name = "node_name", length = 200)
    private String nodeName; // 节点名称
    
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData; // 节点输入数据（JSON格式）
    
    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData; // 节点输出数据（JSON格式）
    
    @Column(name = "status", length = 50, nullable = false)
    private String status; // 状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 错误信息
    
    @Column(name = "start_time")
    private LocalDateTime startTime; // 开始时间
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // 结束时间
    
    @Column(name = "duration")
    private Integer duration; // 执行耗时（毫秒）
    
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0; // 重试次数
    
    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
