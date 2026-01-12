package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 安全审计日志实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "user_name", length = 100)
    private String userName;
    
    @Column(name = "operation_type", length = 50, nullable = false)
    private String operationType; // CREATE/UPDATE/DELETE/APPROVE/LOGIN/LOGOUT等
    
    @Column(name = "operation_module", length = 100)
    private String operationModule; // REQUIREMENT/CASE/TEMPLATE等
    
    @Column(name = "operation_target", length = 500)
    private String operationTarget; // 实体名称或ID
    
    @Column(name = "operation_content", columnDefinition = "TEXT")
    private String operationContent; // 操作内容详情
    
    @Column(name = "operation_result", length = 50)
    private String operationResult; // SUCCESS/FAILURE
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 错误信息（如果失败）
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress; // IP地址
    
    @Column(name = "user_agent", length = 500)
    private String userAgent; // 用户代理
    
    @Column(name = "request_url", length = 1000)
    private String requestUrl; // 请求URL
    
    @Column(name = "request_method", length = 10)
    private String requestMethod; // GET/POST/PUT/DELETE
    
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams; // 请求参数（JSON格式）
    
    @Column(name = "response_status")
    private Integer responseStatus; // 响应状态码
    
    @Column(name = "execution_time")
    private Long executionTime; // 执行时间（毫秒）
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
}

