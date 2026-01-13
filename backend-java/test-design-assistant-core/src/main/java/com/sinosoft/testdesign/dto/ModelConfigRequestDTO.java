package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 模型配置请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class ModelConfigRequestDTO {
    
    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 200, message = "模型名称长度不能超过200个字符")
    private String modelName;
    
    /**
     * 模型类型：DeepSeek/豆包/Kimi/千问
     */
    @Size(max = 50, message = "模型类型长度不能超过50个字符")
    private String modelType;
    
    /**
     * API端点
     */
    @Size(max = 500, message = "API端点长度不能超过500个字符")
    private String apiEndpoint;
    
    /**
     * API密钥（加密存储）
     */
    @Size(max = 500, message = "API密钥长度不能超过500个字符")
    private String apiKey;
    
    /**
     * 模型版本
     */
    @Size(max = 50, message = "模型版本长度不能超过50个字符")
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
    @Size(max = 1, message = "启用状态长度不能超过1个字符")
    private String isActive;
    
    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;
    
    /**
     * 每日调用限制
     */
    private Integer dailyLimit;
}

