package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.ModelConfig;

import java.util.List;

/**
 * 智能模型选择服务接口
 * 根据任务类型、性能数据等智能选择最优模型
 *
 * @author sinosoft
 * @date 2026-01-27
 */
public interface IntelligentModelSelectionService {

    /**
     * 根据任务类型选择最优模型
     *
     * @param taskType 任务类型（CASE_GENERATION, UI_SCRIPT_GENERATION, AGENT_CHAT, etc.）
     * @return 最优模型配置
     */
    ModelConfig selectOptimalModel(String taskType);

    /**
     * 根据任务类型和具体场景选择最优模型
     *
     * @param taskType 任务类型
     * @param scenario 场景（SPEED, RELIABILITY, COST, BALANCED）
     * @return 最优模型配置
     */
    ModelConfig selectOptimalModelByScenario(String taskType, String scenario);

    /**
     * 获取任务类型对应的候选模型列表
     *
     * @param taskType 任务类型
     * @return 候选模型列表（按优先级排序）
     */
    List<ModelConfig> getCandidateModels(String taskType);

    /**
     * 获取任务类型的默认模型
     *
     * @param taskType 任务类型
     * @return 默认模型配置
     */
    ModelConfig getDefaultModel(String taskType);

    /**
     * 根据性能数据刷新模型评分
     * 定时任务调用，根据最新的性能数据更新模型评分和推荐
     */
    void refreshModelScores();
}

