package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.service.ModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型配置管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "模型配置管理", description = "模型配置管理相关接口")
@RestController
@RequestMapping("/v1/model-configs")
@RequiredArgsConstructor
public class ModelConfigController {
    
    private final ModelConfigService modelConfigService;
    
    @Operation(summary = "创建模型配置", description = "创建新的模型配置")
    @PostMapping
    public Result<ModelConfig> createModelConfig(@RequestBody ModelConfig modelConfig) {
        return Result.success(modelConfigService.createModelConfig(modelConfig));
    }
    
    @Operation(summary = "查询模型配置列表", description = "分页查询模型配置列表，支持按模型名称、类型和状态搜索")
    @GetMapping
    public Result<Page<ModelConfig>> getModelConfigList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String modelType,
            @RequestParam(required = false) String isActive) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(modelConfigService.getModelConfigList(pageable, modelName, modelType, isActive));
    }
    
    @Operation(summary = "获取模型配置详情", description = "根据ID获取模型配置详情")
    @GetMapping("/{id}")
    public Result<ModelConfig> getModelConfigById(@PathVariable Long id) {
        return Result.success(modelConfigService.getModelConfigById(id));
    }
    
    @Operation(summary = "根据模型编码获取配置", description = "根据模型编码获取模型配置详情")
    @GetMapping("/code/{modelCode}")
    public Result<ModelConfig> getModelConfigByCode(@PathVariable String modelCode) {
        return Result.success(modelConfigService.getModelConfigByCode(modelCode));
    }
    
    @Operation(summary = "更新模型配置", description = "更新模型配置信息")
    @PutMapping("/{id}")
    public Result<ModelConfig> updateModelConfig(
            @PathVariable Long id,
            @RequestBody ModelConfig modelConfig) {
        return Result.success(modelConfigService.updateModelConfig(id, modelConfig));
    }
    
    @Operation(summary = "删除模型配置", description = "删除指定模型配置")
    @DeleteMapping("/{id}")
    public Result<Void> deleteModelConfig(@PathVariable Long id) {
        modelConfigService.deleteModelConfig(id);
        return Result.success();
    }
    
    @Operation(summary = "启用/禁用模型配置", description = "更新模型配置的启用状态")
    @PutMapping("/{id}/status")
    public Result<ModelConfig> toggleModelConfigStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        return Result.success(modelConfigService.toggleModelConfigStatus(id, isActive));
    }
    
    @Operation(summary = "获取所有启用的模型配置", description = "获取所有启用的模型配置，按优先级排序")
    @GetMapping("/active")
    public Result<List<ModelConfig>> getActiveModelConfigs() {
        return Result.success(modelConfigService.getActiveModelConfigs());
    }
    
    @Operation(summary = "根据类型获取启用的模型配置", description = "根据模型类型获取启用的模型配置")
    @GetMapping("/active/type/{modelType}")
    public Result<List<ModelConfig>> getActiveModelConfigsByType(@PathVariable String modelType) {
        return Result.success(modelConfigService.getActiveModelConfigsByType(modelType));
    }
}

