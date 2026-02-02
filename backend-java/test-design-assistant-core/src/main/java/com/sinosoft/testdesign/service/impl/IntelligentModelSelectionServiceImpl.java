package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.repository.AppLogRepository;
import com.sinosoft.testdesign.repository.ModelConfigRepository;
import com.sinosoft.testdesign.service.IntelligentModelSelectionService;
import com.sinosoft.testdesign.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能模型选择服务实现
 *
 * @author sinosoft
 * @date 2026-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentModelSelectionServiceImpl implements IntelligentModelSelectionService {

    private final ModelConfigRepository modelConfigRepository;
    private final AppLogRepository appLogRepository;
    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    @Override
    public ModelConfig selectOptimalModel(String taskType) {
        return selectOptimalModelByScenario(taskType, "BALANCED");
    }

    @Override
    public ModelConfig selectOptimalModelByScenario(String taskType, String scenario) {
        log.info("选择最优模型 - 任务类型: {}, 场景: {}", taskType, scenario);

        // 获取任务类型的候选模型
        List<ModelConfig> candidateModels = getCandidateModels(taskType);

        if (candidateModels.isEmpty()) {
            log.warn("没有找到支持任务类型 {} 的模型", taskType);
            return null;
        }

        // 根据场景选择模型
        switch (scenario) {
            case "SPEED":
                // 速度优先：选择性能评分中响应时间权重最高的模型
                return candidateModels.stream()
                        .max(Comparator.comparing(m -> m.getPerformanceScore() != null ? m.getPerformanceScore() : BigDecimal.ZERO))
                        .orElse(candidateModels.get(0));

            case "RELIABILITY":
                // 可靠性优先：选择成功率最高的模型
                return candidateModels.stream()
                        .filter(m -> "1".equals(m.getIsRecommended()))
                        .findFirst()
                        .orElse(candidateModels.stream()
                                .sorted((a, b) -> b.getPriority().compareTo(a.getPriority()))
                                .findFirst()
                                .orElse(candidateModels.get(0)));

            case "COST":
                // 成本优先：选择优先级最高且启用的模型
                return candidateModels.stream()
                        .sorted(Comparator.comparing(ModelConfig::getPriority))
                        .findFirst()
                        .orElse(candidateModels.get(0));

            case "BALANCED":
            default:
                // 综合优先：选择推荐模型
                ModelConfig recommendedModel = candidateModels.stream()
                        .filter(m -> "1".equals(m.getIsRecommended()))
                        .findFirst()
                        .orElse(null);

                if (recommendedModel != null) {
                    log.info("选择推荐模型: {}", recommendedModel.getModelCode());
                    return recommendedModel;
                }

                // 如果没有推荐模型，选择性能评分最高的
                return candidateModels.stream()
                        .max(Comparator.comparing(m -> m.getPerformanceScore() != null ? m.getPerformanceScore() : BigDecimal.ZERO))
                        .orElse(candidateModels.get(0));
        }
    }

    @Override
    public List<ModelConfig> getCandidateModels(String taskType) {
        // 获取所有启用的模型
        List<ModelConfig> allModels = modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");

        // 过滤出支持该任务类型的模型
        return allModels.stream()
                .filter(model -> supportsTaskType(model, taskType))
                .sorted(Comparator.comparing(ModelConfig::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public ModelConfig getDefaultModel(String taskType) {
        List<ModelConfig> candidateModels = getCandidateModels(taskType);
        return candidateModels.isEmpty() ? null : candidateModels.get(0);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 */6 * * *")  // 每6小时执行一次
    public void refreshModelScores() {
        log.info("开始刷新模型性能评分");

        try {
            // 获取所有启用的模型
            List<ModelConfig> models = modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");

            for (ModelConfig model : models) {
                try {
                    // 计算模型性能评分
                    BigDecimal performanceScore = calculateModelPerformanceScore(model.getModelCode());

                    // 更新模型配置
                    model.setPerformanceScore(performanceScore);
                    model.setLastScoreUpdateTime(LocalDateTime.now());
                    modelConfigRepository.save(model);

                    log.info("更新模型 {} 性能评分: {}", model.getModelCode(), performanceScore);
                } catch (Exception e) {
                    log.error("计算模型 {} 性能评分失败: {}", model.getModelCode(), e.getMessage());
                }
            }

            // 更新推荐模型标记
            updateRecommendedModels(models);

            log.info("模型性能评分刷新完成");
        } catch (Exception e) {
            log.error("刷新模型性能评分失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查模型是否支持指定的任务类型
     */
    private boolean supportsTaskType(ModelConfig model, String taskType) {
        String taskTypes = model.getTaskTypes();
        if (taskTypes == null || taskTypes.isEmpty()) {
            // 如果没有配置任务类型，默认支持所有任务
            return true;
        }

        try {
            // 解析任务类型JSON
            List<String> supportedTypes = objectMapper.readValue(taskTypes, new TypeReference<List<String>>() {});
            return supportedTypes.contains(taskType);
        } catch (Exception e) {
            log.warn("解析模型 {} 的任务类型失败: {}", model.getModelCode(), e.getMessage());
            return true; // 解析失败时默认支持
        }
    }

    /**
     * 计算模型性能评分
     * 综合考虑成功率、响应时间、成本等因素
     */
    private BigDecimal calculateModelPerformanceScore(String modelCode) {
        try {
            // 获取最近24小时的性能数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(24);

            Map<String, Object> performanceStats = monitoringService.getPerformanceStats(
                    startTime, endTime, modelCode, null, null);

            if (performanceStats == null || performanceStats.isEmpty()) {
                // 如果没有性能数据，返回默认分数
                return BigDecimal.valueOf(70.00);  // 默认70分
            }

            // 提取性能指标
            Long totalCount = ((Number) performanceStats.getOrDefault("totalCount", 0)).longValue();
            if (totalCount == 0) {
                return BigDecimal.valueOf(70.00);
            }

            Double successRate = ((Number) performanceStats.getOrDefault("successRate", 0.0)).doubleValue();
            Double avgResponseTime = ((Number) performanceStats.getOrDefault("avgResponseTime", 3000.0)).doubleValue();

            // 计算评分（总分100分）
            // 成功率权重40%，响应时间权重60%
            double scoreRate = successRate * 0.4;

            // 响应时间分数（0-100分）
            // 假设3000ms为基准，响应时间越短分数越高
            double scoreTime = 0.0;
            if (avgResponseTime > 0) {
                scoreTime = Math.max(0, (3000 - avgResponseTime) / 3000 * 100 * 0.6);
            }

            BigDecimal totalScore = BigDecimal.valueOf(scoreRate + scoreTime)
                    .setScale(2, RoundingMode.HALF_UP);

            log.debug("模型 {} 性能评分 - 成功率: {}%, 响应时间: {}ms, 总分: {}",
                    modelCode, successRate, avgResponseTime, totalScore);

            return totalScore;

        } catch (Exception e) {
            log.error("计算模型 {} 性能评分失败: {}", modelCode, e.getMessage());
            return BigDecimal.valueOf(70.00);
        }
    }

    /**
     * 更新推荐模型标记
     * 每个任务类型只推荐一个性能评分最高的模型
     */
    private void updateRecommendedModels(List<ModelConfig> models) {
        // 先清除所有推荐标记
        models.forEach(model -> model.setIsRecommended("0"));

        // 按任务类型分组
        Map<String, List<ModelConfig>> modelsByTaskType = models.stream()
                .collect(Collectors.groupingBy(model -> {
                    // 返回第一个支持的任务类型作为分组键
                    String taskTypes = model.getTaskTypes();
                    if (taskTypes != null && !taskTypes.isEmpty()) {
                        try {
                            List<String> types = objectMapper.readValue(taskTypes, new TypeReference<List<String>>() {});
                            return types.isEmpty() ? "DEFAULT" : types.get(0);
                        } catch (Exception e) {
                            return "DEFAULT";
                        }
                    }
                    return "DEFAULT";
                }));

        // 每个任务类型选择评分最高的模型作为推荐
        for (Map.Entry<String, List<ModelConfig>> entry : modelsByTaskType.entrySet()) {
            List<ModelConfig> taskModels = entry.getValue();
            if (taskModels.isEmpty()) {
                continue;
            }

            // 找到性能评分最高的模型
            ModelConfig bestModel = taskModels.stream()
                    .max(Comparator.comparing(m -> m.getPerformanceScore() != null ? m.getPerformanceScore() : BigDecimal.ZERO))
                    .orElse(taskModels.get(0));

            bestModel.setIsRecommended("1");
            modelConfigRepository.save(bestModel);

            log.info("任务类型 {} 推荐模型: {} (评分: {})",
                    entry.getKey(), bestModel.getModelCode(), bestModel.getPerformanceScore());
        }
    }
}

