package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.config.FeatureFlagConfig;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.metrics.BusinessMetricsCollector;
import com.sinosoft.testdesign.service.AIServiceClient;
import com.sinosoft.testdesign.service.ModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI服务客户端实现
 * 支持模型降级策略：主模型不可用时自动切换到备用模型
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceClientImpl implements AIServiceClient {
    
    private final RestTemplate restTemplate;
    private final ModelConfigService modelConfigService;
    private final BusinessMetricsCollector metricsCollector;
    private final FeatureFlagConfig featureFlagConfig;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    public Map<String, Object> post(String url, Object request) {
        return postWithFallback(url, request, null);
    }
    
    /**
     * 带降级策略的POST请求
     * 
     * @param url 请求URL
     * @param request 请求体
     * @param preferredModelCode 首选模型代码（可选）
     * @return 响应结果
     */
    public Map<String, Object> postWithFallback(String url, Object request, String preferredModelCode) {
        // 如果未启用降级策略，直接调用
        if (!featureFlagConfig.isModelFallbackEnabled()) {
            return doPost(url, request);
        }
        
        // 获取模型列表（按优先级排序）
        List<ModelConfig> activeModels = modelConfigService.getActiveModelConfigs();
        if (activeModels.isEmpty()) {
            log.error("没有可用的模型配置");
            throw new RuntimeException("没有可用的模型配置");
        }
        
        // 如果指定了首选模型，优先使用
        ModelConfig preferredModel = null;
        if (preferredModelCode != null) {
            preferredModel = activeModels.stream()
                    .filter(m -> preferredModelCode.equals(m.getModelCode()))
                    .findFirst()
                    .orElse(null);
        }
        
        // 构建模型列表：首选模型 + 其他模型
        List<ModelConfig> modelsToTry = preferredModel != null 
                ? List.of(preferredModel) 
                : activeModels;
        
        // 如果首选模型不在列表开头，需要调整顺序
        if (preferredModel != null && !activeModels.isEmpty() && 
            !activeModels.get(0).getModelCode().equals(preferredModelCode)) {
            modelsToTry = new java.util.ArrayList<>(activeModels);
            modelsToTry.remove(preferredModel);
            modelsToTry.add(0, preferredModel);
        }
        
        // 尝试每个模型
        Exception lastException = null;
        for (ModelConfig model : modelsToTry) {
            try {
                log.debug("尝试使用模型: {}", model.getModelCode());
                
                // 创建请求副本，避免修改原始请求
                Object requestToSend = request;
                if (request instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> requestMap = (Map<String, Object>) request;
                    // 创建副本
                    Map<String, Object> requestCopy = new java.util.HashMap<>(requestMap);
                    requestCopy.put("model_code", model.getModelCode());
                    requestToSend = requestCopy;
                }
                
                Map<String, Object> result = doPost(url, requestToSend);
                
                // 如果使用了备用模型，记录降级事件
                if (preferredModel != null && !model.getModelCode().equals(preferredModelCode)) {
                    log.warn("主模型 {} 不可用，已降级到备用模型 {}", preferredModelCode, model.getModelCode());
                    metricsCollector.recordLlmCallFailed(preferredModelCode, 0, "fallback_to_" + model.getModelCode());
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("模型 {} 调用失败: {}", model.getModelCode(), e.getMessage());
                
                // 记录失败指标
                metricsCollector.recordLlmCallFailed(model.getModelCode(), 0, e.getClass().getSimpleName());
                
                // 继续尝试下一个模型
            }
        }
        
        // 所有模型都失败
        log.error("所有模型调用都失败");
        if (lastException != null) {
            throw new RuntimeException("所有模型调用都失败: " + lastException.getMessage(), lastException);
        }
        throw new RuntimeException("所有模型调用都失败");
    }
    
    /**
     * 执行POST请求
     */
    private Map<String, Object> doPost(String url, Object request) {
        log.debug("调用AI服务: POST {}", url);
        try {
            return restTemplate.postForObject(url, request, Map.class);
        } catch (RestClientException e) {
            log.error("AI服务调用失败: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public Map<String, Object> get(String url) {
        log.debug("调用AI服务: GET {}", url);
        return restTemplate.getForObject(url, Map.class);
    }
}

