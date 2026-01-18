package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.PageElementInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 页面元素信息服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface PageElementService {
    
    /**
     * 创建页面元素信息
     */
    PageElementInfo createPageElement(PageElementInfo element);
    
    /**
     * 更新页面元素信息
     */
    PageElementInfo updatePageElement(Long id, PageElementInfo element);
    
    /**
     * 根据ID查询页面元素信息
     */
    PageElementInfo getPageElementById(Long id);
    
    /**
     * 根据元素编码查询页面元素信息
     */
    PageElementInfo getPageElementByCode(String elementCode);
    
    /**
     * 根据页面URL查询元素列表
     */
    List<PageElementInfo> getPageElementsByUrl(String pageUrl);
    
    /**
     * 分页查询页面元素信息列表
     */
    Page<PageElementInfo> getPageElementList(Pageable pageable, String pageUrl, String elementType);
    
    /**
     * 删除页面元素信息
     */
    void deletePageElement(Long id);
}

