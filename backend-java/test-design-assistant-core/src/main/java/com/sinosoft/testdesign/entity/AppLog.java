package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用日志实体
 * 记录所有模型调用和应用操作的详细信息，用于LLMOps监控和分析
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_log", indexes = {
    @Index(name = "idx_app_log_request_id", columnList = "request_id"),
    @Index(name = "idx_app_log_timestamp", columnList = "timestamp"),
    @Index(name = "idx_app_log_user_id", columnList = "user_id"),
    @Index(name = "idx_app_log_app_type", columnList = "app_type"),
    @Index(name = "idx_app_log_model_code", columnList = "model_code"),
    @Index(name = "idx_app_log_status", columnList = "status")
})
public class AppLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", length = 100, unique = true, nullable = false)
    private String requestId; // 唯一请求ID
    
    @Column(name = "user_id")
    private Long userId; // 用户ID
    
    @Column(name = "user_name", length = 100)
    private String userName; // 用户名
    
    @Column(name = "app_type", length = 50)
    private String appType; // 应用类型：CASE_GENERATION/UI_SCRIPT_GENERATION/DOCUMENT_PARSING等
    
    @Column(name = "model_code", length = 50)
    private String modelCode; // 模型代码
    
    @Column(name = "model_name", length = 200)
    private String modelName; // 模型名称
    
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt; // 提示词
    
    @Column(name = "prompt_length")
    private Integer promptLength; // 提示词长度
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response; // 模型响应
    
    @Column(name = "response_length")
    private Integer responseLength; // 响应长度
    
    @Column(name = "tokens_input")
    private Integer tokensInput; // 输入token数
    
    @Column(name = "tokens_output")
    private Integer tokensOutput; // 输出token数
    
    @Column(name = "tokens_total")
    private Integer tokensTotal; // 总token数
    
    @Column(name = "response_time")
    private Integer responseTime; // 响应时间（毫秒）
    
    @Column(name = "cost", precision = 10, scale = 6)
    private BigDecimal cost; // 成本（元）
    
    @Column(name = "status", length = 20, nullable = false)
    private String status; // 状态：success/failed
    
    @Column(name = "error", columnDefinition = "TEXT")
    private String error; // 错误信息
    
    @Column(name = "error_code", length = 50)
    private String errorCode; // 错误代码
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress; // IP地址
    
    @Column(name = "user_agent", length = 500)
    private String userAgent; // 用户代理
    
    @Column(name = "request_url", length = 1000)
    private String requestUrl; // 请求URL
    
    @Column(name = "request_method", length = 10)
    private String requestMethod; // 请求方法
    
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams; // 请求参数（JSON格式）
    
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData; // 响应数据（JSON格式）
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // 元数据（JSON格式）
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp; // 时间戳
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "success";
        }
    }
}
