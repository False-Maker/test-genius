package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 页面元素信息实体
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
@Entity
@Table(name = "page_element_info")
public class PageElementInfo {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 元素编码
     */
    @Column(name = "element_code", unique = true, nullable = false, length = 100)
    private String elementCode;
    
    /**
     * 页面URL
     */
    @Column(name = "page_url", nullable = false, length = 1000)
    private String pageUrl;
    
    /**
     * 元素类型：BUTTON/INPUT/LINK/SELECT等
     */
    @Column(name = "element_type", length = 50)
    private String elementType;
    
    /**
     * 定位方式：ID/CLASS/XPATH/CSS_SELECTOR
     */
    @Column(name = "element_locator_type", length = 50)
    private String elementLocatorType;
    
    /**
     * 定位值
     */
    @Column(name = "element_locator_value", length = 500)
    private String elementLocatorValue;
    
    /**
     * 元素文本
     */
    @Column(name = "element_text", length = 500)
    private String elementText;
    
    /**
     * 元素属性（JSON格式）
     */
    @Column(name = "element_attributes", columnDefinition = "TEXT")
    private String elementAttributes;
    
    /**
     * 页面结构（JSON格式）
     */
    @Column(name = "page_structure", columnDefinition = "TEXT")
    private String pageStructure;
    
    /**
     * 截图URL
     */
    @Column(name = "screenshot_url", length = 1000)
    private String screenshotUrl;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

