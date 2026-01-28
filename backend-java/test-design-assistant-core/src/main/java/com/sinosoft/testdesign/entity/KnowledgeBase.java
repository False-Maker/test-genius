package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库实体类
 * 用于存储知识库的基本信息
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 知识库编码（唯一）
     * 格式：KB-YYYYMMDD-序号
     */
    @Column(name = "kb_code", unique = true, nullable = false, length = 100)
    private String kbCode;
    
    /**
     * 知识库名称
     */
    @Column(name = "kb_name", nullable = false, length = 500)
    private String kbName;
    
    /**
     * 知识库描述
     */
    @Column(name = "kb_description", columnDefinition = "TEXT")
    private String kbDescription;
    
    /**
     * 知识库类型
     * public-公共知识库/private-私有知识库/project-项目知识库
     */
    @Column(name = "kb_type", length = 50)
    private String kbType;
    
    /**
     * 嵌入模型
     * 如：text-embedding-ada-002, text-embedding-3-small
     */
    @Column(name = "embedding_model", length = 100)
    private String embeddingModel;
    
    /**
     * 分块策略
     * paragraph/sentence/fixed_size/semantic/recursive
     */
    @Column(name = "chunking_strategy", length = 50)
    private String chunkingStrategy;
    
    /**
     * 分块大小
     */
    @Column(name = "chunk_size")
    private Integer chunkSize;
    
    /**
     * 分块重叠大小
     */
    @Column(name = "chunk_overlap")
    private Integer chunkOverlap;
    
    /**
     * 是否激活
     * 0-未激活/1-已激活
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
    
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
     * 创建前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新前自动更新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

