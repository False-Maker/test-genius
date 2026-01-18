package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试执行记录实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "test_execution_record")
public class TestExecutionRecord {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 执行记录编码（REC-YYYYMMDD-序号）
     */
    @Column(name = "record_code", unique = true, nullable = false, length = 100)
    private String recordCode;
    
    /**
     * 执行任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 关联用例ID
     */
    @Column(name = "case_id")
    private Long caseId;
    
    /**
     * 执行类型：MANUAL/AUTOMATED
     */
    @Column(name = "execution_type", nullable = false, length = 50)
    private String executionType;
    
    /**
     * 执行状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
     */
    @Column(name = "execution_status", nullable = false, length = 50)
    private String executionStatus = "PENDING";
    
    /**
     * 执行结果详情
     */
    @Column(name = "execution_result", columnDefinition = "TEXT")
    private String executionResult;
    
    /**
     * 执行日志
     */
    @Column(name = "execution_log", columnDefinition = "TEXT")
    private String executionLog;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_duration")
    private Integer executionDuration;
    
    /**
     * 执行人ID
     */
    @Column(name = "executed_by")
    private Long executedBy;
    
    /**
     * 执行人姓名
     */
    @Column(name = "executed_by_name", length = 100)
    private String executedByName;
    
    /**
     * 执行时间
     */
    @Column(name = "execution_time")
    private LocalDateTime executionTime;
    
    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private LocalDateTime finishTime;
    
    /**
     * 截图URL（失败时）
     */
    @Column(name = "screenshot_url", length = 1000)
    private String screenshotUrl;
    
    /**
     * 视频URL（可选）
     */
    @Column(name = "video_url", length = 1000)
    private String videoUrl;
    
    @PrePersist
    public void prePersist() {
        if (this.executionTime == null) {
            this.executionTime = LocalDateTime.now();
        }
    }
}

