package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库权限响应DTO
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class KnowledgePermissionResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 知识库ID
     */
    private Long kbId;
    
    /**
     * 知识库编码
     */
    private String kbCode;
    
    /**
     * 知识库名称
     */
    private String kbName;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 权限类型
     * read-读取/write-写入/admin-管理员
     */
    private String permissionType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

