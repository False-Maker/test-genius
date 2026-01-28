package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent消息实体（对话历史）
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "agent_message")
public class AgentMessage {
    
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
     * 消息类型
     * USER - 用户消息
     * ASSISTANT - Agent回复
     * TOOL - 工具调用消息
     * SYSTEM - 系统消息
     */
    @Column(name = "message_type", nullable = false, length = 50)
    private String messageType;
    
    /**
     * 角色
     * user, assistant, tool, system
     */
    @Column(name = "role", nullable = false, length = 50)
    private String role;
    
    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 工具调用信息（JSON格式）
     */
    @Column(name = "tool_calls", columnDefinition = "JSONB")
    private String toolCalls;
    
    /**
     * 工具执行结果（JSON格式）
     */
    @Column(name = "tool_results", columnDefinition = "JSONB")
    private String toolResults;
    
    /**
     * Token使用量
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed = 0;
    
    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time")
    private Integer responseTime;
    
    /**
     * 使用的模型代码
     */
    @Column(name = "model_code", length = 50)
    private String modelCode;
    
    /**
     * 迭代次数
     */
    @Column(name = "iteration_number")
    private Integer iterationNumber;
    
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

