package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 页面元素信息请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class PageElementInfoRequestDTO {
    
    /**
     * 页面URL
     */
    @NotBlank(message = "页面URL不能为空")
    @Size(max = 1000, message = "页面URL长度不能超过1000个字符")
    private String pageUrl;
    
    /**
     * 元素类型：BUTTON/INPUT/LINK/SELECT等
     */
    @Size(max = 50, message = "元素类型长度不能超过50个字符")
    private String elementType;
    
    /**
     * 定位方式：ID/CLASS/XPATH/CSS_SELECTOR
     */
    @Size(max = 50, message = "定位方式长度不能超过50个字符")
    private String elementLocatorType;
    
    /**
     * 定位值
     */
    @Size(max = 500, message = "定位值长度不能超过500个字符")
    private String elementLocatorValue;
    
    /**
     * 元素文本
     */
    @Size(max = 500, message = "元素文本长度不能超过500个字符")
    private String elementText;
    
    /**
     * 元素属性（JSON格式）
     */
    private String elementAttributes;
    
    /**
     * 页面结构（JSON格式）
     */
    private String pageStructure;
    
    /**
     * 截图URL
     */
    @Size(max = 1000, message = "截图URL长度不能超过1000个字符")
    private String screenshotUrl;
}

