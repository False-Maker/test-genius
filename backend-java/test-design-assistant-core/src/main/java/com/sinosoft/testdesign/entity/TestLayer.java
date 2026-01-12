package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 测试分层实体
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@Entity
@Table(name = "test_layer")
public class TestLayer {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分层编码
     */
    @Column(name = "layer_code", unique = true, nullable = false, length = 50)
    private String layerCode;
    
    /**
     * 分层名称
     */
    @Column(name = "layer_name", nullable = false, length = 100)
    private String layerName;
    
    /**
     * 分层描述
     */
    @Column(name = "layer_description", columnDefinition = "TEXT")
    private String layerDescription;
    
    /**
     * 排序顺序
     */
    @Column(name = "layer_order")
    private Integer layerOrder;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", length = 1)
    private String isActive = "1";
}

