package com.sinosoft.testdesign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 功能开关配置
 * 用于控制系统的功能开关，支持功能降级
 * 
 * @author sinosoft
 * @date 2024-01-13
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.feature-flag")
public class FeatureFlagConfig {
    
    /**
     * 是否启用模型降级策略
     * 默认：true
     */
    private boolean modelFallbackEnabled = true;
    
    /**
     * 是否启用用例生成功能
     * 默认：true
     */
    private boolean caseGenerationEnabled = true;
    
    /**
     * 是否启用用例质量评估功能
     * 默认：true
     */
    private boolean caseQualityAssessmentEnabled = true;
    
    /**
     * 是否启用用例复用功能
     * 默认：true
     */
    private boolean caseReuseEnabled = true;
    
    /**
     * 是否启用知识库功能
     * 默认：true
     */
    private boolean knowledgeBaseEnabled = true;
    
    /**
     * 系统负载阈值（CPU使用率百分比）
     * 超过此阈值时，自动关闭非核心功能
     * 默认：80
     */
    private int systemLoadThreshold = 80;
    
    /**
     * 是否启用自动降级
     * 当系统负载超过阈值时，自动关闭非核心功能
     * 默认：false（需要手动配置）
     */
    private boolean autoDegradeEnabled = false;
}

