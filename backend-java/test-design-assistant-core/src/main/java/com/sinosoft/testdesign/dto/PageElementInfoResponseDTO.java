package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 页面元素信息响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class PageElementInfoResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 元素编码
     */
    private String elementCode;
    
    /**
     * 页面URL
     */
    private String pageUrl;
    
    /**
     * 元素类型
     */
    private String elementType;
    
    /**
     * 定位方式
     */
    private String elementLocatorType;
    
    /**
     * 定位值
     */
    private String elementLocatorValue;
    
    /**
     * 元素文本
     */
    private String elementText;
    
    /**
     * 元素属性
     */
    private String elementAttributes;
    
    /**
     * 页面结构
     */
    private String pageStructure;
    
    /**
     * 截图URL
     */
    private String screenshotUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

