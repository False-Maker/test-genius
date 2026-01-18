package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.service.AIServiceClient;
import com.sinosoft.testdesign.service.UIScriptRepairService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UI脚本修复服务实现
 * 集成Python AI服务，提供UI脚本错误分析、页面变化检测和脚本修复功能
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UIScriptRepairServiceImpl implements UIScriptRepairService {
    
    private final AIServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    public Map<String, Object> analyzeError(String errorLog, String scriptContent, boolean useLlm) {
        log.info("开始分析错误日志，使用LLM: {}", useLlm);
        
        try {
            // 构建请求
            Map<String, Object> request = new HashMap<>();
            request.put("error_log", errorLog);
            if (scriptContent != null && !scriptContent.isEmpty()) {
                request.put("script_content", scriptContent);
            }
            request.put("use_llm", useLlm);
            
            // 调用Python AI服务
            String url = aiServiceUrl + "/api/v1/ui-script/analyze-error";
            Map<String, Object> response = aiServiceClient.post(url, request);
            
            if (response == null) {
                throw new BusinessException("Python服务返回空响应");
            }
            
            log.info("错误分析完成");
            return response;
            
        } catch (Exception e) {
            log.error("错误分析失败: {}", e.getMessage(), e);
            throw new BusinessException("错误分析失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> detectPageChanges(
            String oldPageCodeUrl,
            List<Map<String, Object>> oldPageElements,
            String newPageCodeUrl,
            List<Map<String, Object>> newPageElements,
            List<Map<String, Object>> scriptLocators) {
        
        log.info("开始检测页面变化");
        
        try {
            // 构建请求
            Map<String, Object> request = new HashMap<>();
            if (oldPageCodeUrl != null && !oldPageCodeUrl.isEmpty()) {
                request.put("old_page_code_url", oldPageCodeUrl);
            }
            if (oldPageElements != null && !oldPageElements.isEmpty()) {
                request.put("old_page_elements", oldPageElements);
            }
            if (newPageCodeUrl != null && !newPageCodeUrl.isEmpty()) {
                request.put("new_page_code_url", newPageCodeUrl);
            }
            if (newPageElements != null && !newPageElements.isEmpty()) {
                request.put("new_page_elements", newPageElements);
            }
            if (scriptLocators != null && !scriptLocators.isEmpty()) {
                request.put("script_locators", scriptLocators);
            }
            
            // 调用Python AI服务
            String url = aiServiceUrl + "/api/v1/ui-script/detect-page-changes";
            Map<String, Object> response = aiServiceClient.post(url, request);
            
            if (response == null) {
                throw new BusinessException("Python服务返回空响应");
            }
            
            log.info("页面变化检测完成");
            return response;
            
        } catch (Exception e) {
            log.error("页面变化检测失败: {}", e.getMessage(), e);
            throw new BusinessException("页面变化检测失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> repairScript(
            String scriptContent,
            String errorLog,
            Map<String, Object> errorAnalysis,
            Map<String, Object> pageChanges,
            String newPageCodeUrl,
            List<Map<String, Object>> newPageElements,
            String scriptType,
            String scriptLanguage,
            boolean useLlm) {
        
        log.info("开始修复UI脚本，脚本类型: {}, 脚本语言: {}, 使用LLM: {}", scriptType, scriptLanguage, useLlm);
        
        // 参数验证
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            throw new BusinessException("脚本内容不能为空");
        }
        
        try {
            // 构建请求
            Map<String, Object> request = new HashMap<>();
            request.put("script_content", scriptContent);
            request.put("error_log", errorLog);
            
            if (errorAnalysis != null && !errorAnalysis.isEmpty()) {
                request.put("error_analysis", errorAnalysis);
            }
            if (pageChanges != null && !pageChanges.isEmpty()) {
                request.put("page_changes", pageChanges);
            }
            if (newPageCodeUrl != null && !newPageCodeUrl.isEmpty()) {
                request.put("new_page_code_url", newPageCodeUrl);
            }
            if (newPageElements != null && !newPageElements.isEmpty()) {
                request.put("new_page_elements", newPageElements);
            }
            
            request.put("script_type", scriptType != null ? scriptType : "SELENIUM");
            request.put("script_language", scriptLanguage != null ? scriptLanguage : "PYTHON");
            request.put("use_llm", useLlm);
            
            // 调用Python AI服务
            String url = aiServiceUrl + "/api/v1/ui-script/repair";
            Map<String, Object> response = aiServiceClient.post(url, request);
            
            if (response == null) {
                throw new BusinessException("Python服务返回空响应");
            }
            
            log.info("脚本修复完成");
            return response;
            
        } catch (Exception e) {
            log.error("脚本修复失败: {}", e.getMessage(), e);
            throw new BusinessException("脚本修复失败: " + e.getMessage());
        }
    }
}

