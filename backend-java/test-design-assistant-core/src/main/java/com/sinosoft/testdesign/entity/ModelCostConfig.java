package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型成本配置实体
 * 配置各模型的token价格，用于成本统计
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "model_cost_config", indexes = {
    @Index(name = "idx_model_cost_config_model_code", columnList = "model_code"),
    @Index(name = "idx_model_cost_config_is_active", columnList = "is_active")
})
public class ModelCostConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "model_code", length = 50, unique = true, nullable = false)
    private String modelCode; // 模型代码
    
    @Column(name = "model_name", length = 200, nullable = false)
    private String modelName; // 模型名称
    
    @Column(name = "input_price_per_1k_tokens", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal inputPricePer1kTokens = BigDecimal.ZERO; // 输入token价格（每1k tokens，单位：元）
    
    @Column(name = "output_price_per_1k_tokens", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal outputPricePer1kTokens = BigDecimal.ZERO; // 输出token价格（每1k tokens，单位：元）
    
    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "CNY"; // 货币单位
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 是否启用
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 描述
    
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
        if (currency == null) {
            currency = "CNY";
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
