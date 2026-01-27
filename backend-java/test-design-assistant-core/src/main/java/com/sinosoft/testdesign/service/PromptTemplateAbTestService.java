package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.PromptTemplateAbTest;
import com.sinosoft.testdesign.entity.PromptTemplateAbTestExecution;
import com.sinosoft.testdesign.entity.PromptTemplateVersion;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板A/B测试服务接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
public interface PromptTemplateAbTestService {
    
    /**
     * 创建A/B测试
     */
    PromptTemplateAbTest createAbTest(Long templateId, PromptTemplateAbTest abTest);
    
    /**
     * 根据ID查询A/B测试
     */
    PromptTemplateAbTest getAbTestById(Long id);
    
    /**
     * 根据模板ID查询所有A/B测试
     */
    List<PromptTemplateAbTest> getAbTestsByTemplateId(Long templateId);
    
    /**
     * 启动A/B测试
     */
    PromptTemplateAbTest startAbTest(Long id);
    
    /**
     * 暂停A/B测试
     */
    PromptTemplateAbTest pauseAbTest(Long id);
    
    /**
     * 停止A/B测试
     */
    PromptTemplateAbTest stopAbTest(Long id);
    
    /**
     * 删除A/B测试
     */
    void deleteAbTest(Long id);
    
    /**
     * 选择版本（根据流量分配逻辑）
     * 返回 'A' 或 'B'
     */
    String selectVersion(Long abTestId, String requestId);
    
    /**
     * 记录A/B测试执行
     */
    PromptTemplateAbTestExecution recordExecution(PromptTemplateAbTestExecution execution);
    
    /**
     * 获取A/B测试统计信息
     */
    Map<String, Object> getAbTestStatistics(Long abTestId);
    
    /**
     * 自动选择最优版本
     */
    PromptTemplateVersion autoSelectBestVersion(Long abTestId);
    
    /**
     * 检查是否需要自动选择版本
     */
    boolean shouldAutoSelect(Long abTestId);
}
