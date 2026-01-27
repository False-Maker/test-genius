package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警记录实体
 * 记录触发的告警信息
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alert_record", indexes = {
    @Index(name = "idx_alert_record_rule_id", columnList = "rule_id"),
    @Index(name = "idx_alert_record_rule_code", columnList = "rule_code"),
    @Index(name = "idx_alert_record_alert_level", columnList = "alert_level"),
    @Index(name = "idx_alert_record_alert_time", columnList = "alert_time"),
    @Index(name = "idx_alert_record_is_resolved", columnList = "is_resolved")
})
public class AlertRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_id", nullable = false)
    private Long ruleId; // 告警规则ID
    
    @Column(name = "rule_code", length = 100, nullable = false)
    private String ruleCode; // 告警规则代码
    
    @Column(name = "alert_level", length = 20, nullable = false)
    private String alertLevel; // 告警级别：INFO/WARNING/ERROR/CRITICAL
    
    @Column(name = "alert_title", length = 200, nullable = false)
    private String alertTitle; // 告警标题
    
    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage; // 告警消息
    
    @Column(name = "current_value", precision = 10, scale = 2)
    private BigDecimal currentValue; // 当前值
    
    @Column(name = "threshold_value", precision = 10, scale = 2)
    private BigDecimal thresholdValue; // 阈值
    
    @Column(name = "target_scope", length = 50)
    private String targetScope; // 目标范围
    
    @Column(name = "target_value", length = 200)
    private String targetValue; // 目标值
    
    @Column(name = "alert_time")
    private LocalDateTime alertTime; // 告警时间
    
    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false; // 是否已解决
    
    @Column(name = "resolved_by")
    private Long resolvedBy; // 解决人ID
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt; // 解决时间
    
    @Column(name = "resolved_note", columnDefinition = "TEXT")
    private String resolvedNote; // 解决说明
    
    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = false; // 是否已发送通知
    
    @Column(name = "notification_channels", columnDefinition = "TEXT")
    private String notificationChannels; // 已发送的通知渠道
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", insertable = false, updatable = false)
    private AlertRule alertRule; // 关联的告警规则
    
    @PrePersist
    protected void onCreate() {
        if (alertTime == null) {
            alertTime = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isResolved == null) {
            isResolved = false;
        }
        if (notificationSent == null) {
            notificationSent = false;
        }
    }
}
