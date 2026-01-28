package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent定义实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "agent")
public class Agent {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Agent编码
     */
    @Column(name = "agent_code", unique = true, nullable = false, length = 100)
    private String agentCode;
    
    /**
     * Agent名称
     */
    @Column(name = "agent_name", nullable = false, length = 200)
    private String agentName;
    
    /**
     * Agent类型
     * TEST_DESIGN_ASSISTANT - 智能测试设计助手
     * CASE_OPTIMIZATION - 测试用例优化Agent
     * CUSTOM - 自定义Agent
     */
    @Column(name = "agent_type", nullable = false, length = 50)
    private String agentType;
    
    /**
     * Agent描述
     */
    @Column(name = "agent_description", columnDefinition = "TEXT")
    private String agentDescription;
    
    /**
     * Agent配置（JSON格式）
     * 包含：模型配置、提示词配置、工具列表等
     */
    @Column(name = "agent_config", columnDefinition = "JSONB")
    private String agentConfig;
    
    /**
     * 系统提示词
     */
    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;
    
    /**
     * 最大迭代次数
     */
    @Column(name = "max_iterations")
    private Integer maxIterations = 10;
    
    /**
     * 最大token数
     */
    @Column(name = "max_tokens")
    private Integer maxTokens = 4000;
    
    /**
     * 温度参数
     */
    @Column(name = "temperature", precision = 3, scale = 2)
    private Double temperature = 0.7;
    
    /**
     * 是否启用
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
    
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

