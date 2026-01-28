package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库响应DTO
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class KnowledgeBaseResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 知识库编码
     */
    private String kbCode;
    
    /**
     * 知识库名称
     */
    private String kbName;
    
    /**
     * 知识库描述
     */
    private String kbDescription;
    
    /**
     * 知识库类型
     */
    private String kbType;
    
    /**
     * 嵌入模型
     */
    private String embeddingModel;
    
    /**
     * 分块策略
     */
    private String chunkingStrategy;
    
    /**
     * 分块大小
     */
    private Integer chunkSize;
    
    /**
     * 分块重叠大小
     */
    private Integer chunkOverlap;
    
    /**
     * 是否激活
     */
    private String isActive;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 文档数量
     */
    private Integer documentCount;
    
    /**
     * 分块数量
     */
    private Integer chunkCount;
    
    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;
}

