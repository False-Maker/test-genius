package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用例生成任务实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "case_generation_task")
public class CaseGenerationTask {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务编码
     */
    @Column(name = "task_code", unique = true, nullable = false, length = 100)
    private String taskCode;
    
    /**
     * 需求ID
     */
    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;
    
    /**
     * 测试分层ID
     */
    @Column(name = "layer_id")
    private Long layerId;
    
    /**
     * 测试方法ID
     */
    @Column(name = "method_id")
    private Long methodId;
    
    /**
     * 提示词模板ID
     */
    @Column(name = "template_id")
    private Long templateId;
    
    /**
     * 模型编码
     */
    @Column(name = "model_code", length = 100)
    private String modelCode;
    
    /**
     * 任务状态：PENDING/PROCESSING/SUCCESS/FAILED
     */
    @Column(name = "task_status", length = 50)
    private String taskStatus = "PENDING";
    
    /**
     * 进度（0-100）
     */
    @Column(name = "progress")
    private Integer progress = 0;
    
    /**
     * 总用例数
     */
    @Column(name = "total_cases")
    private Integer totalCases = 0;
    
    /**
     * 成功用例数
     */
    @Column(name = "success_cases")
    private Integer successCases = 0;
    
    /**
     * 失败用例数
     */
    @Column(name = "fail_cases")
    private Integer failCases = 0;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 结果数据（JSON格式）
     */
    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 完成时间
     */
    @Column(name = "complete_time")
    private LocalDateTime completeTime;
    
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

