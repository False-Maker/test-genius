package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent工具调用记录实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "agent_tool_call")
public class AgentToolCall {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 会话ID
     */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    /**
     * 消息ID
     */
    @Column(name = "message_id")
    private Long messageId;
    
    /**
     * 工具编码
     */
    @Column(name = "tool_code", nullable = false, length = 100)
    private String toolCode;
    
    /**
     * 工具名称
     */
    @Column(name = "tool_name", nullable = false, length = 200)
    private String toolName;
    
    /**
     * 调用参数（JSON格式）
     */
    @Column(name = "call_arguments", nullable = false, columnDefinition = "JSONB")
    private String callArguments;
    
    /**
     * 调用结果（JSON格式）
     */
    @Column(name = "call_result", columnDefinition = "JSONB")
    private String callResult;
    
    /**
     * 调用状态
     * SUCCESS - 成功
     * FAILED - 失败
     * TIMEOUT - 超时
     */
    @Column(name = "call_status", nullable = false, length = 50)
    private String callStatus;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 执行时间（毫秒）
     */
    @Column(name = "execution_time")
    private Integer executionTime;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

