package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.service.IntelligentModelSelectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能模型选择控制器
 *
 * @author sinosoft
 * @date 2026-01-27
 */
@Tag(name = "智能模型选择", description = "智能模型选择相关接口")
@RestController
@RequestMapping("/v1/intelligent-model")
@RequiredArgsConstructor
public class IntelligentModelSelectionController {

    private final IntelligentModelSelectionService intelligentModelSelectionService;

    @Operation(summary = "根据任务类型选择最优模型", description = "自动选择综合评分最高的模型")
    @GetMapping("/select-optimal")
    public Result<ModelConfig> selectOptimalModel(
            @RequestParam String taskType) {
        ModelConfig model = intelligentModelSelectionService.selectOptimalModel(taskType);
        return Result.success(model);
    }

    @Operation(summary = "根据任务类型和场景选择最优模型", description = "支持场景：SPEED（速度优先）、RELIABILITY（可靠性优先）、COST（成本优先）、BALANCED（综合优先）")
    @GetMapping("/select-by-scenario")
    public Result<ModelConfig> selectOptimalModelByScenario(
            @RequestParam String taskType,
            @RequestParam(defaultValue = "BALANCED") String scenario) {
        ModelConfig model = intelligentModelSelectionService.selectOptimalModelByScenario(taskType, scenario);
        return Result.success(model);
    }

    @Operation(summary = "获取任务类型的候选模型列表", description = "获取支持指定任务类型的所有模型，按优先级排序")
    @GetMapping("/candidate-models")
    public Result<List<ModelConfig>> getCandidateModels(
            @RequestParam String taskType) {
        List<ModelConfig> models = intelligentModelSelectionService.getCandidateModels(taskType);
        return Result.success(models);
    }

    @Operation(summary = "获取任务类型的默认模型", description = "获取优先级最高的模型作为默认模型")
    @GetMapping("/default-model")
    public Result<ModelConfig> getDefaultModel(
            @RequestParam String taskType) {
        ModelConfig model = intelligentModelSelectionService.getDefaultModel(taskType);
        return Result.success(model);
    }

    @Operation(summary = "手动刷新模型评分", description = "根据最新的性能数据更新所有模型的评分")
    @PostMapping("/refresh-scores")
    public Result<Void> refreshScores() {
        intelligentModelSelectionService.refreshModelScores();
        return Result.success(null, "模型评分刷新成功");
    }
}

