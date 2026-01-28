package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库同步日志响应DTO
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class KnowledgeBaseSyncLogResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 知识库ID
     */
    private Long kbId;
    
    /**
     * 同步类型
     * incremental-增量同步/full-全量同步
     */
    private String syncType;
    
    /**
     * 源文件路径
     */
    private String sourcePath;
    
    /**
     * 新增文档数量
     */
    private Integer addedCount;
    
    /**
     * 更新文档数量
     */
    private Integer updatedCount;
    
    /**
     * 删除文档数量
     */
    private Integer deletedCount;
    
    /**
     * 失败文档数量
     */
    private Integer failedCount;
    
    /**
     * 同步状态
     * pending-待处理/running-运行中/success-成功/failed-失败
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 耗时（秒）
     */
    private Long duration;
}

