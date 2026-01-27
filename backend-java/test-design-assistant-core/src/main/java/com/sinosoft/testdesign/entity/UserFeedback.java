package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 * 记录用户对模型响应质量的反馈
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_feedback", indexes = {
    @Index(name = "idx_user_feedback_request_id", columnList = "request_id"),
    @Index(name = "idx_user_feedback_log_id", columnList = "log_id"),
    @Index(name = "idx_user_feedback_user_id", columnList = "user_id"),
    @Index(name = "idx_user_feedback_rating", columnList = "rating"),
    @Index(name = "idx_user_feedback_feedback_time", columnList = "feedback_time")
})
public class UserFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", length = 100, nullable = false)
    private String requestId; // 关联的请求ID
    
    @Column(name = "log_id")
    private Long logId; // 关联的日志ID
    
    @Column(name = "user_id")
    private Long userId; // 用户ID
    
    @Column(name = "user_name", length = 100)
    private String userName; // 用户名
    
    @Column(name = "rating")
    private Integer rating; // 评分：1-5分
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment; // 反馈内容
    
    @Column(name = "feedback_type", length = 50)
    private String feedbackType; // 反馈类型：POSITIVE/NEGATIVE/NEUTRAL
    
    @Column(name = "tags", length = 500)
    private String tags; // 标签（逗号分隔）
    
    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    private Boolean isResolved = false; // 是否已处理
    
    @Column(name = "resolved_by")
    private Long resolvedBy; // 处理人ID
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt; // 处理时间
    
    @Column(name = "feedback_time")
    private LocalDateTime feedbackTime; // 反馈时间
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 创建时间
    
    @PrePersist
    protected void onCreate() {
        if (feedbackTime == null) {
            feedbackTime = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isResolved == null) {
            isResolved = false;
        }
    }
}
