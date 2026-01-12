package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.PromptTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 提示词模板管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface PromptTemplateService {
    
    /**
     * 创建模板
     */
    PromptTemplate createTemplate(PromptTemplate template);
    
    /**
     * 更新模板
     */
    PromptTemplate updateTemplate(Long id, PromptTemplate template);
    
    /**
     * 根据ID查询模板
     */
    PromptTemplate getTemplateById(Long id);
    
    /**
     * 分页查询模板列表
     */
    Page<PromptTemplate> getTemplateList(Pageable pageable);
    
    /**
     * 删除模板
     */
    void deleteTemplate(Long id);
    
    /**
     * 启用/禁用模板
     */
    PromptTemplate toggleTemplateStatus(Long id, String isActive);
    
    /**
     * 根据模板ID和变量生成提示词
     */
    String generatePrompt(Long templateId, Map<String, Object> variables);
    
    /**
     * 根据模板内容和变量生成提示词
     */
    String generatePrompt(String templateContent, Map<String, Object> variables);
}

