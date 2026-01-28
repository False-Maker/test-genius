package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库权限请求DTO
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class KnowledgePermissionRequestDTO {
    
    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private Long kbId;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 权限类型
     * read-读取/write-写入/admin-管理员
     */
    @NotBlank(message = "权限类型不能为空")
    @Size(max = 20, message = "权限类型长度不能超过20个字符")
    private String permissionType;
}

