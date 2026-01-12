package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 测试设计方法实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "test_design_method")
public class TestDesignMethod {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 方法编码
     */
    @Column(name = "method_code", unique = true, nullable = false, length = 50)
    private String methodCode;
    
    /**
     * 方法名称
     */
    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName;
    
    /**
     * 方法描述
     */
    @Column(name = "method_description", columnDefinition = "TEXT")
    private String methodDescription;
    
    /**
     * 适用的测试分层（逗号分隔）
     */
    @Column(name = "applicable_layers", length = 500)
    private String applicableLayers;
    
    /**
     * 使用示例
     */
    @Column(name = "example", columnDefinition = "TEXT")
    private String example;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
}

