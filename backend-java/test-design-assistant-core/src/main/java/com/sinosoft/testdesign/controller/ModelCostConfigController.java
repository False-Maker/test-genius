package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.ModelCostConfig;
import com.sinosoft.testdesign.service.ModelCostConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 模型成本配置控制器
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Tag(name = "模型成本配置", description = "模型成本配置管理相关接口")
@RestController
@RequestMapping("/v1/model-cost-configs")
@RequiredArgsConstructor
public class ModelCostConfigController {
    
    private final ModelCostConfigService costConfigService;
    
    @Operation(summary = "创建成本配置", description = "创建新的模型成本配置")
    @PostMapping
    public Result<ModelCostConfig> createCostConfig(@Valid @RequestBody ModelCostConfig config) {
        ModelCostConfig saved = costConfigService.createCostConfig(config);
        return Result.success(saved);
    }
    
    @Operation(summary = "更新成本配置", description = "更新模型成本配置")
    @PutMapping("/{id}")
    public Result<ModelCostConfig> updateCostConfig(
            @PathVariable Long id,
            @Valid @RequestBody ModelCostConfig config) {
        config.setId(id);
        ModelCostConfig updated = costConfigService.updateCostConfig(config);
        return Result.success(updated);
    }
    
    @Operation(summary = "查询成本配置", description = "根据ID查询成本配置")
    @GetMapping("/{id}")
    public Result<ModelCostConfig> getCostConfig(@PathVariable Long id) {
        Optional<ModelCostConfig> config = costConfigService.findById(id);
        return config.map(Result::success)
                .orElse(Result.error("成本配置不存在"));
    }
    
    @Operation(summary = "根据模型代码查询成本配置", description = "根据模型代码查询成本配置")
    @GetMapping("/model/{modelCode}")
    public Result<ModelCostConfig> getCostConfigByModelCode(@PathVariable String modelCode) {
        Optional<ModelCostConfig> config = costConfigService.findByModelCode(modelCode);
        return config.map(Result::success)
                .orElse(Result.error("成本配置不存在"));
    }
    
    @Operation(summary = "查询所有启用的成本配置", description = "查询所有启用的成本配置列表")
    @GetMapping("/active")
    public Result<List<ModelCostConfig>> getActiveCostConfigs() {
        List<ModelCostConfig> configs = costConfigService.findAllActive();
        return Result.success(configs);
    }
    
    @Operation(summary = "查询所有成本配置", description = "查询所有成本配置列表")
    @GetMapping
    public Result<List<ModelCostConfig>> getAllCostConfigs() {
        List<ModelCostConfig> configs = costConfigService.findAll();
        return Result.success(configs);
    }
    
    @Operation(summary = "删除成本配置", description = "根据ID删除成本配置")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCostConfig(@PathVariable Long id) {
        costConfigService.deleteById(id);
        return Result.success();
    }
    
    @Operation(summary = "启用/禁用成本配置", description = "启用或禁用成本配置")
    @PutMapping("/{id}/toggle-active")
    public Result<ModelCostConfig> toggleActive(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        ModelCostConfig config = costConfigService.toggleActive(id, isActive);
        return Result.success(config);
    }
}
