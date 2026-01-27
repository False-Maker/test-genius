package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.PromptTemplateVersion;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板版本管理服务接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
public interface PromptTemplateVersionService {
    
    /**
     * 创建新版本
     */
    PromptTemplateVersion createVersion(Long templateId, PromptTemplateVersion version);
    
    /**
     * 根据ID查询版本
     */
    PromptTemplateVersion getVersionById(Long id);
    
    /**
     * 根据模板ID查询所有版本
     */
    List<PromptTemplateVersion> getVersionsByTemplateId(Long templateId);
    
    /**
     * 根据模板ID和版本号查询版本
     */
    PromptTemplateVersion getVersionByTemplateIdAndVersionNumber(Long templateId, Integer versionNumber);
    
    /**
     * 获取当前版本
     */
    PromptTemplateVersion getCurrentVersion(Long templateId);
    
    /**
     * 版本回滚
     */
    PromptTemplateVersion rollbackToVersion(Long templateId, Integer versionNumber);
    
    /**
     * 版本对比
     */
    Map<String, Object> compareVersions(Long templateId, Integer versionNumber1, Integer versionNumber2);
    
    /**
     * 删除版本
     */
    void deleteVersion(Long id);
}
