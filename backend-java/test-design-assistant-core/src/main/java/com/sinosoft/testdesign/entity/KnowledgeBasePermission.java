package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库权限实体类
 * 用于存储知识库的访问权限信息
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
@Entity
@Table(name = "knowledge_base_permission")
public class KnowledgeBasePermission {
    
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
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 权限类型
     * read-读取/write-写入/admin-管理员
     */
    @Column(name = "permission_type", nullable = false, length = 20)
    private String permissionType;
    
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

