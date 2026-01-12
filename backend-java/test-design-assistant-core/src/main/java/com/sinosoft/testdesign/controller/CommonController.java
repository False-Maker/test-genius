package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.entity.TestDesignMethod;
import com.sinosoft.testdesign.entity.TestLayer;
import com.sinosoft.testdesign.repository.ModelConfigRepository;
import com.sinosoft.testdesign.repository.TestLayerRepository;
import com.sinosoft.testdesign.repository.TestMethodRepository;
import com.sinosoft.testdesign.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用数据控制器
 * 提供基础数据查询接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "通用数据", description = "通用基础数据查询接口")
@RestController
@RequestMapping("/v1/common")
@RequiredArgsConstructor
public class CommonController {
    
    private final TestLayerRepository testLayerRepository;
    private final TestMethodRepository testMethodRepository;
    private final ModelConfigRepository modelConfigRepository;
    private final CacheService cacheService;
    
    // 缓存键
    private static final String CACHE_KEY_TEST_LAYERS = "cache:common:test-layers";
    private static final String CACHE_KEY_TEST_METHODS = "cache:common:test-methods";
    private static final String CACHE_KEY_MODEL_CONFIGS = "cache:common:model-configs";
    
    @Operation(summary = "获取测试分层列表", description = "获取所有启用的测试分层列表")
    @GetMapping("/test-layers")
    public Result<List<TestLayer>> getTestLayerList() {
        // 尝试从缓存获取
        List<TestLayer> cached = cacheService.getList(CACHE_KEY_TEST_LAYERS, TestLayer.class);
        if (cached != null) {
            return Result.success(cached);
        }
        
        // 从数据库查询
        List<TestLayer> layers = testLayerRepository.findByIsActiveOrderByLayerOrder("1");
        
        // 存入缓存（1小时）
        cacheService.set(CACHE_KEY_TEST_LAYERS, layers, 3600);
        
        return Result.success(layers);
    }
    
    @Operation(summary = "获取测试设计方法列表", description = "获取所有启用的测试设计方法列表")
    @GetMapping("/test-design-methods")
    public Result<List<TestDesignMethod>> getTestDesignMethodList() {
        // 尝试从缓存获取
        List<TestDesignMethod> cached = cacheService.getList(CACHE_KEY_TEST_METHODS, TestDesignMethod.class);
        if (cached != null) {
            return Result.success(cached);
        }
        
        // 从数据库查询
        List<TestDesignMethod> methods = testMethodRepository.findByIsActive("1");
        
        // 存入缓存（1小时）
        cacheService.set(CACHE_KEY_TEST_METHODS, methods, 3600);
        
        return Result.success(methods);
    }
    
    @Operation(summary = "获取模型配置列表", description = "获取所有启用的模型配置列表")
    @GetMapping("/model-configs")
    public Result<List<ModelConfig>> getModelConfigList() {
        // 尝试从缓存获取
        List<ModelConfig> cached = cacheService.getList(CACHE_KEY_MODEL_CONFIGS, ModelConfig.class);
        if (cached != null) {
            return Result.success(cached);
        }
        
        // 从数据库查询
        List<ModelConfig> models = modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");
        
        // 存入缓存（30分钟）
        cacheService.set(CACHE_KEY_MODEL_CONFIGS, models, 1800);
        
        return Result.success(models);
    }
}

