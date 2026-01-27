package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板A/B测试配置实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "prompt_template_ab_test")
public class PromptTemplateAbTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;
    
    @Column(name = "test_description", columnDefinition = "TEXT")
    private String testDescription;
    
    @Column(name = "version_a_id", nullable = false)
    private Long versionAId;
    
    @Column(name = "version_b_id", nullable = false)
    private Long versionBId;
    
    @Column(name = "traffic_split_a")
    private Integer trafficSplitA = 50;
    
    @Column(name = "traffic_split_b")
    private Integer trafficSplitB = 50;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "status", length = 20)
    private String status = "draft";  // draft/running/paused/completed
    
    @Column(name = "auto_select_enabled", length = 1)
    private String autoSelectEnabled = "0";
    
    @Column(name = "min_samples")
    private Integer minSamples = 100;
    
    @Column(name = "selection_criteria", length = 50)
    private String selectionCriteria = "success_rate";  // success_rate/response_time/user_rating
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_by_name", length = 100)
    private String createdByName;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.updateTime == null) {
            this.updateTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
