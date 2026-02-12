package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.repository.ModelConfigRepository;
import com.sinosoft.testdesign.service.ModelCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 模型调用服务实现
 *
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelCallServiceImpl implements ModelCallService {

    private final ModelConfigRepository modelConfigRepository;
    private final RestTemplate restTemplate;

    @Override
    public Optional<ModelConfig> getBestModelForTask(String taskType) {
        // 1. 按任务类型查找启用的模型（taskTypes JSON 字段中包含该类型）
        List<ModelConfig> taskModels = modelConfigRepository
                .findByIsActiveAndTaskTypesContainingOrderByPriorityAsc("1", taskType);

        if (!taskModels.isEmpty()) {
            // 优先选推荐模型
            for (ModelConfig config : taskModels) {
                if ("1".equals(config.getIsRecommended())) {
                    log.info("使用推荐模型（任务匹配）: modelCode={}, modelName={}, taskType={}",
                            config.getModelCode(), config.getModelName(), taskType);
                    return Optional.of(config);
                }
            }
            // 没有推荐的，取优先级最高的
            ModelConfig config = taskModels.get(0);
            log.info("使用任务匹配模型: modelCode={}, modelName={}, taskType={}",
                    config.getModelCode(), config.getModelName(), taskType);
            return Optional.of(config);
        }

        // 2. 兜底：返回优先级最高的活跃模型
        List<ModelConfig> allActive = modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");
        if (!allActive.isEmpty()) {
            // 优先选推荐模型
            for (ModelConfig config : allActive) {
                if ("1".equals(config.getIsRecommended())) {
                    log.info("使用推荐模型（兜底）: modelCode={}, modelName={}", config.getModelCode(), config.getModelName());
                    return Optional.of(config);
                }
            }
            ModelConfig config = allActive.get(0);
            log.info("使用兜底模型（优先级最高）: modelCode={}, modelName={}", config.getModelCode(), config.getModelName());
            return Optional.of(config);
        }

        log.warn("未找到任何可用的模型配置, taskType={}", taskType);
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> callWithModel(ModelConfig modelConfig, String url, Map<String, Object> requestBody) {
        // 将模型配置信息注入请求体
        Map<String, Object> enrichedRequest = new HashMap<>(requestBody);

        Map<String, Object> modelInfo = new HashMap<>();
        modelInfo.put("model_code", modelConfig.getModelCode());
        modelInfo.put("model_name", modelConfig.getModelName());
        modelInfo.put("model_type", modelConfig.getModelType());
        modelInfo.put("api_endpoint", modelConfig.getApiEndpoint());
        modelInfo.put("api_key", modelConfig.getApiKey());
        modelInfo.put("model_version", modelConfig.getModelVersion());
        modelInfo.put("max_tokens", modelConfig.getMaxTokens());
        modelInfo.put("temperature", modelConfig.getTemperature());
        enrichedRequest.put("model_config", modelInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(enrichedRequest, headers);

        log.info("调用AI服务: url={}, modelCode={}, modelType={}",
                url, modelConfig.getModelCode(), modelConfig.getModelType());

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        return Map.of("success", false, "message", "AI服务响应异常");
    }
}
