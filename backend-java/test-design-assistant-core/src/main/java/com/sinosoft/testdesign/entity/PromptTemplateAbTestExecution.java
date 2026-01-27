package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板A/B测试执行记录实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "prompt_template_ab_test_execution")
public class PromptTemplateAbTestExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ab_test_id", nullable = false)
    private Long abTestId;
    
    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;
    
    @Column(name = "version_id", nullable = false)
    private Long versionId;
    
    @Column(name = "version_label", nullable = false, length = 10)
    private String versionLabel;  // A或B
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "response_time")
    private Integer responseTime;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "status", length = 20)
    private String status = "success";
    
    @Column(name = "error", columnDefinition = "TEXT")
    private String error;
    
    @Column(name = "user_rating")
    private Integer userRating;  // 1-5分
    
    @Column(name = "user_feedback", columnDefinition = "TEXT")
    private String userFeedback;
    
    @Column(name = "execution_time")
    private LocalDateTime executionTime;
    
    @PrePersist
    public void prePersist() {
        if (this.executionTime == null) {
            this.executionTime = LocalDateTime.now();
        }
    }
}
