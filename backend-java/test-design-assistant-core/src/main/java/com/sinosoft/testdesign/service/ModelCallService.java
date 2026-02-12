package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.ModelConfig;

import java.util.Map;
import java.util.Optional;

/**
 * 模型调用服务接口
 *
 * @author sinosoft
 * @date 2024-01-01
 */
public interface ModelCallService {

    /**
     * 获取适用于指定任务类型的最佳模型配置
     * 优先返回推荐模型，其次按任务类型匹配，最后按优先级兜底
     *
     * @param taskType 任务类型，如 "REQUIREMENT_ANALYSIS"、"CASE_GENERATION" 等
     * @return 模型配置（可能为空）
     */
    Optional<ModelConfig> getBestModelForTask(String taskType);

    /**
     * 使用指定模型配置调用 AI 服务
     *
     * @param modelConfig 模型配置
     * @param url         请求的 Python AI 服务 URL
     * @param requestBody 请求体
     * @return AI 服务响应
     */
    Map<String, Object> callWithModel(ModelConfig modelConfig, String url, Map<String, Object> requestBody);
}
