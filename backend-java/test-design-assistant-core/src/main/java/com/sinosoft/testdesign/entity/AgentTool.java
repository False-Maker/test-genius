package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent工具实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "agent_tool")
public class AgentTool {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 工具编码
     */
    @Column(name = "tool_code", unique = true, nullable = false, length = 100)
    private String toolCode;
    
    /**
     * 工具名称
     */
    @Column(name = "tool_name", nullable = false, length = 200)
    private String toolName;
    
    /**
     * 工具类型
     * TEST_RELATED - 测试相关工具
     * GENERAL - 通用工具
     * CUSTOM - 自定义工具
     */
    @Column(name = "tool_type", nullable = false, length = 50)
    private String toolType;
    
    /**
     * 工具描述
     */
    @Column(name = "tool_description", nullable = false, columnDefinition = "TEXT")
    private String toolDescription;
    
    /**
     * 工具Schema（OpenAPI格式，JSON）
     */
    @Column(name = "tool_schema", nullable = false, columnDefinition = "JSONB")
    private String toolSchema;
    
    /**
     * 工具实现路径
     * 格式：模块.类.方法 或 函数路径
     */
    @Column(name = "tool_implementation", length = 500)
    private String toolImplementation;
    
    /**
     * 工具配置（JSON格式）
     */
    @Column(name = "tool_config", columnDefinition = "JSONB")
    private String toolConfig;
    
    /**
     * 是否内置工具
     */
    @Column(name = "is_builtin", length = 1)
    private String isBuiltin = "0";
    
    /**
     * 是否启用
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
    
    /**
     * 权限级别
     * NORMAL - 普通权限
     * ADMIN - 管理员权限
     */
    @Column(name = "permission_level", length = 50)
    private String permissionLevel = "NORMAL";
    
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
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

