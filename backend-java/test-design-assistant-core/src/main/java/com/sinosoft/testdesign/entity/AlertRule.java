package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警规则实体
 * 定义各种告警规则，用于监控系统状态
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alert_rule", indexes = {
    @Index(name = "idx_alert_rule_rule_code", columnList = "rule_code"),
    @Index(name = "idx_alert_rule_rule_type", columnList = "rule_type"),
    @Index(name = "idx_alert_rule_is_enabled", columnList = "is_enabled")
})
public class AlertRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_code", length = 100, unique = true, nullable = false)
    private String ruleCode; // 规则代码
    
    @Column(name = "rule_name", length = 200, nullable = false)
    private String ruleName; // 规则名称
    
    @Column(name = "rule_type", length = 50, nullable = false)
    private String ruleType; // 规则类型：FAILURE_RATE/RESPONSE_TIME/COST等
    
    @Column(name = "alert_condition", length = 50, nullable = false)
    private String alertCondition; // 告警条件：GT/GTE/LT/LTE/EQ
    
    @Column(name = "threshold_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal thresholdValue; // 阈值
    
    @Column(name = "threshold_unit", length = 20)
    private String thresholdUnit; // 阈值单位：PERCENT/MS/CNY等
    
    @Column(name = "time_window")
    @Builder.Default
    private Integer timeWindow = 300; // 时间窗口（秒）
    
    @Column(name = "check_interval")
    @Builder.Default
    private Integer checkInterval = 60; // 检查间隔（秒）
    
    @Column(name = "target_scope", length = 50)
    private String targetScope; // 目标范围：ALL/MODEL/APP/USER
    
    @Column(name = "target_value", length = 200)
    private String targetValue; // 目标值（模型代码、应用类型、用户ID等）
    
    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true; // 是否启用
    
    @Column(name = "notification_channels", columnDefinition = "TEXT")
    private String notificationChannels; // 通知渠道（JSON格式）
    
    @Column(name = "notification_recipients", columnDefinition = "TEXT")
    private String notificationRecipients; // 通知接收人（JSON格式）
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 描述
    
    @Column(name = "creator_id")
    private Long creatorId; // 创建人ID
    
    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间
    
    @Column(name = "update_time")
    private LocalDateTime updateTime; // 更新时间
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (isEnabled == null) {
            isEnabled = true;
        }
        if (timeWindow == null) {
            timeWindow = 300;
        }
        if (checkInterval == null) {
            checkInterval = 60;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
