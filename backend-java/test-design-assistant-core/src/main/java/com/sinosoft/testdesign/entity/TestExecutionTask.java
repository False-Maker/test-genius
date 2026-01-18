package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试执行任务实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "test_execution_task")
public class TestExecutionTask {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务编码（TASK-YYYYMMDD-序号）
     */
    @Column(name = "task_code", unique = true, nullable = false, length = 100)
    private String taskCode;
    
    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, length = 500)
    private String taskName;
    
    /**
     * 任务类型：AUTO_SCRIPT_GENERATION/AUTO_SCRIPT_REPAIR/MANUAL_EXECUTION
     */
    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;
    
    /**
     * 关联需求ID
     */
    @Column(name = "requirement_id")
    private Long requirementId;
    
    /**
     * 关联用例ID
     */
    @Column(name = "case_id")
    private Long caseId;
    
    /**
     * 关联测试套件ID
     */
    @Column(name = "case_suite_id")
    private Long caseSuiteId;
    
    /**
     * 脚本类型：SELENIUM/PLAYWRIGHT/PUPPETEER
     */
    @Column(name = "script_type", length = 50)
    private String scriptType;
    
    /**
     * 脚本内容
     */
    @Column(name = "script_content", columnDefinition = "TEXT")
    private String scriptContent;
    
    /**
     * 脚本语言：PYTHON/JAVA/JAVASCRIPT
     */
    @Column(name = "script_language", length = 50)
    private String scriptLanguage;
    
    /**
     * 页面代码URL（文件路径或URL）
     */
    @Column(name = "page_code_url", length = 1000)
    private String pageCodeUrl;
    
    /**
     * 自然语言描述（用于脚本生成）
     */
    @Column(name = "natural_language_desc", columnDefinition = "TEXT")
    private String naturalLanguageDesc;
    
    /**
     * 错误日志（用于脚本修复）
     */
    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;
    
    /**
     * 执行配置（JSON格式）
     */
    @Column(name = "execution_config", columnDefinition = "TEXT")
    private String executionConfig;
    
    /**
     * 任务状态：PENDING/PROCESSING/SUCCESS/FAILED
     */
    @Column(name = "task_status", nullable = false, length = 50)
    private String taskStatus = "PENDING";
    
    /**
     * 任务进度（0-100）
     */
    @Column(name = "progress")
    private Integer progress = 0;
    
    /**
     * 成功数量
     */
    @Column(name = "success_count")
    private Integer successCount = 0;
    
    /**
     * 失败数量
     */
    @Column(name = "fail_count")
    private Integer failCount = 0;
    
    /**
     * 结果数据（JSON格式）
     */
    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Column(name = "creator_name", length = 100)
    private String creatorName;
    
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
    @Column(name = "finish_time")
    private LocalDateTime finishTime;
    
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

