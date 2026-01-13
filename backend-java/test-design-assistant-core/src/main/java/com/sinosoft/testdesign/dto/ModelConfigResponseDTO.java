package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型配置响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class ModelConfigResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 模型编码
     */
    private String modelCode;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型类型：DeepSeek/豆包/Kimi/千问
     */
    private String modelType;
    
    /**
     * API端点
     */
    private String apiEndpoint;
    
    /**
     * 模型版本
     */
    private String modelVersion;
    
    /**
     * 最大Token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    private BigDecimal temperature;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
    
    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;
    
    /**
     * 每日调用限制
     */
    private Integer dailyLimit;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 注意：API密钥不包含在响应DTO中，避免泄露敏感信息
     */
}

