package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent会话实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "agent_session")
public class AgentSession {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 会话编码
     */
    @Column(name = "session_code", unique = true, nullable = false, length = 100)
    private String sessionCode;
    
    /**
     * Agent ID
     */
    @Column(name = "agent_id", nullable = false)
    private Long agentId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 用户姓名
     */
    @Column(name = "user_name", length = 100)
    private String userName;
    
    /**
     * 会话标题
     */
    @Column(name = "session_title", length = 500)
    private String sessionTitle;
    
    /**
     * 上下文数据（JSON格式）
     * 包含：对话历史、工具调用历史等
     */
    @Column(name = "context_data", columnDefinition = "JSONB")
    private String contextData;
    
    /**
     * 会话状态
     * ACTIVE - 活跃
     * CLOSED - 已关闭
     * EXPIRED - 已过期
     */
    @Column(name = "status", length = 50)
    private String status = "ACTIVE";
    
    /**
     * 总token使用量
     */
    @Column(name = "total_tokens")
    private Integer totalTokens = 0;
    
    /**
     * 总迭代次数
     */
    @Column(name = "total_iterations")
    private Integer totalIterations = 0;
    
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
     * 最后活跃时间
     */
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        lastActiveTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
        lastActiveTime = LocalDateTime.now();
    }
}

