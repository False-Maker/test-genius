package com.sinosoft.testdesign.service;

import java.util.Map;

/**
 * UI脚本修复服务接口
 * 集成Python AI服务，提供UI脚本错误分析、页面变化检测和脚本修复功能
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface UIScriptRepairService {
    
    /**
     * 分析错误日志
     * 
     * @param errorLog 错误日志
     * @param scriptContent 脚本内容（可选）
     * @param useLlm 是否使用大语言模型分析
     * @return 错误分析结果
     */
    Map<String, Object> analyzeError(String errorLog, String scriptContent, boolean useLlm);
    
    /**
     * 检测页面变化
     * 
     * @param oldPageCodeUrl 旧页面代码URL或文件路径（可选）
     * @param oldPageElements 旧页面元素列表（可选）
     * @param newPageCodeUrl 新页面代码URL或文件路径（可选）
     * @param newPageElements 新页面元素列表（可选）
     * @param scriptLocators 脚本定位器列表（可选）
     * @return 页面变化检测结果
     */
    Map<String, Object> detectPageChanges(
            String oldPageCodeUrl,
            java.util.List<Map<String, Object>> oldPageElements,
            String newPageCodeUrl,
            java.util.List<Map<String, Object>> newPageElements,
            java.util.List<Map<String, Object>> scriptLocators
    );
    
    /**
     * 修复UI脚本
     * 
     * @param scriptContent 原始脚本内容
     * @param errorLog 错误日志
     * @param errorAnalysis 错误分析结果（可选，如果不提供则自动分析）
     * @param pageChanges 页面变化检测结果（可选，如果不提供则自动检测）
     * @param newPageCodeUrl 新页面代码URL或文件路径（可选）
     * @param newPageElements 新页面元素列表（可选）
     * @param scriptType 脚本类型（SELENIUM/PLAYWRIGHT）
     * @param scriptLanguage 脚本语言（PYTHON/JAVA/JAVASCRIPT）
     * @param useLlm 是否使用大语言模型修复
     * @return 修复结果
     */
    Map<String, Object> repairScript(
            String scriptContent,
            String errorLog,
            Map<String, Object> errorAnalysis,
            Map<String, Object> pageChanges,
            String newPageCodeUrl,
            java.util.List<Map<String, Object>> newPageElements,
            String scriptType,
            String scriptLanguage,
            boolean useLlm
    );
}

