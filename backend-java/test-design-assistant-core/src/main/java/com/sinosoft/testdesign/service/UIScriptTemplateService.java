package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.UIScriptTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * UI脚本模板服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface UIScriptTemplateService {
    
    /**
     * 创建脚本模板
     */
    UIScriptTemplate createTemplate(UIScriptTemplate template);
    
    /**
     * 更新脚本模板
     */
    UIScriptTemplate updateTemplate(Long id, UIScriptTemplate template);
    
    /**
     * 根据ID查询脚本模板
     */
    UIScriptTemplate getTemplateById(Long id);
    
    /**
     * 根据模板编码查询脚本模板
     */
    UIScriptTemplate getTemplateByCode(String templateCode);
    
    /**
     * 分页查询脚本模板列表
     */
    Page<UIScriptTemplate> getTemplateList(Pageable pageable, String templateName, 
            String templateType, String scriptLanguage, String isActive);
    
    /**
     * 根据模板类型和脚本语言查询启用的模板列表
     */
    List<UIScriptTemplate> getActiveTemplatesByTypeAndLanguage(String templateType, String scriptLanguage);
    
    /**
     * 启用/禁用模板
     */
    UIScriptTemplate updateTemplateStatus(Long id, String isActive);
    
    /**
     * 删除脚本模板
     */
    void deleteTemplate(Long id);
}

