package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型配置实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "model_config")
public class ModelConfig {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模型编码
     */
    @Column(name = "model_code", unique = true, nullable = false, length = 100)
    private String modelCode;
    
    /**
     * 模型名称
     */
    @Column(name = "model_name", nullable = false, length = 200)
    private String modelName;
    
    /**
     * 模型类型：DeepSeek/豆包/Kimi/千问/智谱
     */
    @Column(name = "model_type", length = 50)
    private String modelType;
    
    /**
     * API端点
     */
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    /**
     * API密钥（加密存储）
     */
    @Column(name = "api_key", length = 500)
    private String apiKey;
    
    /**
     * 模型版本
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;
    
    /**
     * 最大Token数
     */
    @Column(name = "max_tokens")
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    @Column(name = "temperature", precision = 3, scale = 2)
    private BigDecimal temperature;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", columnDefinition = "CHAR(1)")
    private String isActive = "1";
    
    /**
     * 优先级（数字越小优先级越高）
     */
    @Column(name = "priority")
    private Integer priority;
    
    /**
     * 每日调用限制
     */
    @Column(name = "daily_limit")
    private Integer dailyLimit;
    
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
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

