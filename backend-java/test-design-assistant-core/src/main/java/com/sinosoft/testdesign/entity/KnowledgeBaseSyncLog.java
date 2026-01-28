package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库同步日志实体类
 * 用于记录知识库同步的历史记录
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
@Entity
@Table(name = "knowledge_base_sync_log")
public class KnowledgeBaseSyncLog {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 知识库ID
     */
    @Column(name = "kb_id", nullable = false)
    private Long kbId;
    
    /**
     * 同步类型
     * incremental-增量同步/full-全量同步
     */
    @Column(name = "sync_type", nullable = false, length = 20)
    private String syncType;
    
    /**
     * 源文件路径
     */
    @Column(name = "source_path", length = 1000)
    private String sourcePath;
    
    /**
     * 新增文档数量
     */
    @Column(name = "added_count")
    private Integer addedCount = 0;
    
    /**
     * 更新文档数量
     */
    @Column(name = "updated_count")
    private Integer updatedCount = 0;
    
    /**
     * 删除文档数量
     */
    @Column(name = "deleted_count")
    private Integer deletedCount = 0;
    
    /**
     * 失败文档数量
     */
    @Column(name = "failed_count")
    private Integer failedCount = 0;
    
    /**
     * 同步状态
     * pending-待处理/running-运行中/success-成功/failed-失败
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 创建前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

