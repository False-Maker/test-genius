package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.UIScriptGenerationRequest;
import com.sinosoft.testdesign.dto.UIScriptGenerationResult;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.repository.PageElementInfoRepository;
import com.sinosoft.testdesign.repository.TestExecutionTaskRepository;
import com.sinosoft.testdesign.service.UIScriptGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * UI脚本生成服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UIScriptGenerationServiceImpl implements UIScriptGenerationService {
    
    private final TestExecutionTaskRepository taskRepository;
    private final PageElementInfoRepository pageElementInfoRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    private static final String TASK_CODE_PREFIX = "TASK";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public String generateScript(UIScriptGenerationRequest request) {
        // 验证参数
        if (request == null || !StringUtils.hasText(request.getNaturalLanguageDesc())) {
            throw new BusinessException("自然语言描述不能为空");
        }
        
        log.info("创建UI脚本生成任务，自然语言描述: {}, 脚本类型: {}", 
                request.getNaturalLanguageDesc(), request.getScriptType());
        
        // 生成任务编码
        String taskCode = generateTaskCode();
        
        // 创建任务
        TestExecutionTask task = new TestExecutionTask();
        task.setTaskCode(taskCode);
        task.setTaskName("UI脚本生成任务");
        task.setTaskType("AUTO_SCRIPT_GENERATION");
        task.setRequirementId(request.getRequirementId());
        task.setCaseId(request.getCaseId());
        task.setScriptType(request.getScriptType());
        task.setScriptLanguage(request.getScriptLanguage());
        task.setPageCodeUrl(request.getPageCodeUrl());
        task.setNaturalLanguageDesc(request.getNaturalLanguageDesc());
        task.setTaskStatus("PENDING");
        task.setProgress(0);
        task.setCreatorId(request.getCreatorId());
        task.setCreatorName(request.getCreatorName());
        
        task = taskRepository.save(task);
        
        // 异步执行脚本生成
        executeScriptGenerationAsync(task.getId(), request);
        
        return taskCode;
    }
    
    @Override
    public UIScriptGenerationResult getTaskStatus(String taskCode) {
        TestExecutionTask task = taskRepository.findByTaskCode(taskCode)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskCode));
        
        UIScriptGenerationResult result = new UIScriptGenerationResult();
        result.setTaskCode(task.getTaskCode());
        result.setTaskStatus(task.getTaskStatus());
        result.setProgress(task.getProgress());
        result.setScriptContent(task.getScriptContent());
        result.setScriptType(task.getScriptType());
        result.setScriptLanguage(task.getScriptLanguage());
        result.setPageUrl(extractPageUrlFromResult(task.getResultData()));
        result.setErrorMessage(task.getErrorMessage());
        
        if (task.getCreateTime() != null) {
            result.setCreateTime(task.getCreateTime().toString());
        }
        if (task.getFinishTime() != null) {
            result.setFinishTime(task.getFinishTime().toString());
        }
        
        // 解析结果数据
        if (task.getResultData() != null) {
            try {
                Map<String, Object> resultData = objectMapper.readValue(
                        task.getResultData(), 
                        new TypeReference<Map<String, Object>>() {}
                );
                result.setElementsUsed((List<Map<String, Object>>) resultData.get("elements_used"));
                result.setSteps((List<Map<String, Object>>) resultData.get("steps"));
            } catch (Exception e) {
                log.warn("解析结果数据失败: {}", e.getMessage());
            }
        }
        
        return result;
    }
    
    @Override
    public List<PageElementInfo> parsePageCode(String pageCodeUrl) {
        // 验证URL
        if (pageCodeUrl == null || pageCodeUrl.trim().isEmpty()) {
            throw new BusinessException("页面代码URL不能为空");
        }
        
        try {
            log.info("解析页面代码: {}", pageCodeUrl);
            
            // 调用Python服务解析页面
            Map<String, Object> request = new HashMap<>();
            request.put("page_code_url", pageCodeUrl);
            
            String url = aiServiceUrl + "/api/v1/ui-script/parse-page";
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response == null) {
                throw new BusinessException("Python服务返回空响应");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> elements = (List<Map<String, Object>>) response.get("elements");
            
            // 转换为PageElementInfo实体（这里简化处理，实际可能需要保存到数据库）
            List<PageElementInfo> pageElements = new ArrayList<>();
            if (elements != null) {
                for (Map<String, Object> element : elements) {
                    PageElementInfo info = new PageElementInfo();
                    info.setElementType((String) element.get("element_type"));
                    info.setElementText((String) element.get("element_text"));
                    info.setPageUrl(pageCodeUrl);
                    // 设置定位信息
                    @SuppressWarnings("unchecked")
                    Map<String, String> locators = (Map<String, String>) element.get("locators");
                    if (locators != null) {
                        if (locators.containsKey("id")) {
                            info.setElementLocatorType("ID");
                            info.setElementLocatorValue(locators.get("id"));
                        } else if (locators.containsKey("name")) {
                            info.setElementLocatorType("NAME");
                            info.setElementLocatorValue(locators.get("name"));
                        } else if (locators.containsKey("css_selector")) {
                            info.setElementLocatorType("CSS_SELECTOR");
                            info.setElementLocatorValue(locators.get("css_selector"));
                        }
                    }
                    pageElements.add(info);
                }
            }
            
            return pageElements;
            
        } catch (Exception e) {
            log.error("页面代码解析失败: {}", e.getMessage(), e);
            throw new BusinessException("页面代码解析失败: " + e.getMessage());
        }
    }
    
    @Async
    @Transactional
    public void executeScriptGenerationAsync(Long taskId, UIScriptGenerationRequest request) {
        TestExecutionTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskId));
        
        try {
            task.setTaskStatus("PROCESSING");
            task.setProgress(10);
            task = taskRepository.save(task);
            
            log.info("开始执行UI脚本生成，任务ID: {}", taskId);
            
            // 构建Python服务请求
            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("natural_language_desc", request.getNaturalLanguageDesc());
            if (request.getPageCodeUrl() != null) {
                pythonRequest.put("page_code_url", request.getPageCodeUrl());
            }
            if (request.getPageElements() != null) {
                pythonRequest.put("page_elements", request.getPageElements());
            }
            pythonRequest.put("script_type", request.getScriptType());
            pythonRequest.put("script_language", request.getScriptLanguage());
            if (request.getPageUrl() != null) {
                pythonRequest.put("page_url", request.getPageUrl());
            }
            pythonRequest.put("use_llm", request.getUseLlm());
            
            task.setProgress(30);
            task = taskRepository.save(task);
            
            // 调用Python服务生成脚本
            String url = aiServiceUrl + "/api/v1/ui-script/generate";
            log.info("调用Python服务生成脚本，URL: {}", url);
            Map<String, Object> response = restTemplate.postForObject(url, pythonRequest, Map.class);
            
            if (response == null) {
                throw new BusinessException("Python服务返回空响应");
            }
            
            task.setProgress(80);
            task = taskRepository.save(task);
            
            // 解析响应
            String scriptContent = (String) response.get("script_content");
            String scriptType = (String) response.get("script_type");
            String scriptLanguage = (String) response.get("script_language");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> elementsUsed = (List<Map<String, Object>>) response.get("elements_used");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> steps = (List<Map<String, Object>>) response.get("steps");
            String pageUrl = (String) response.get("page_url");
            
            // 保存结果
            task.setScriptContent(scriptContent);
            task.setScriptType(scriptType);
            task.setScriptLanguage(scriptLanguage);
            
            // 保存结果数据（JSON格式）
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("elements_used", elementsUsed);
            resultData.put("steps", steps);
            resultData.put("page_url", pageUrl);
            task.setResultData(objectMapper.writeValueAsString(resultData));
            
            task.setTaskStatus("SUCCESS");
            task.setProgress(100);
            task.setFinishTime(LocalDateTime.now());
            task.setSuccessCount(1);
            
            taskRepository.save(task);
            
            log.info("UI脚本生成成功，任务ID: {}", taskId);
            
        } catch (Exception e) {
            log.error("UI脚本生成失败，任务ID: {}, 错误: {}", taskId, e.getMessage(), e);
            task.setTaskStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setFinishTime(LocalDateTime.now());
            task.setFailCount(1);
            taskRepository.save(task);
        }
    }
    
    private String generateTaskCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = TASK_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天最大的序号
        List<TestExecutionTask> tasks = taskRepository.findByTaskCodeStartingWithOrderByIdDesc(prefix);
        int sequence = 1;
        if (!tasks.isEmpty()) {
            String lastCode = tasks.get(0).getTaskCode();
            String lastSequence = lastCode.substring(lastCode.lastIndexOf("-") + 1);
            try {
                sequence = Integer.parseInt(lastSequence) + 1;
            } catch (NumberFormatException e) {
                // 如果解析失败，使用1
            }
        }
        
        return prefix + String.format("%04d", sequence);
    }
    
    private String extractPageUrlFromResult(String resultData) {
        if (resultData == null) {
            return null;
        }
        try {
            Map<String, Object> data = objectMapper.readValue(
                    resultData, 
                    new TypeReference<Map<String, Object>>() {}
            );
            return (String) data.get("page_url");
        } catch (Exception e) {
            return null;
        }
    }
}

