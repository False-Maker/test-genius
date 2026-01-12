package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.TestDesignMethod;
import com.sinosoft.testdesign.entity.TestLayer;
import com.sinosoft.testdesign.repository.TestLayerRepository;
import com.sinosoft.testdesign.repository.TestMethodRepository;
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
    
    @Operation(summary = "获取测试分层列表", description = "获取所有启用的测试分层列表")
    @GetMapping("/test-layers")
    public Result<List<TestLayer>> getTestLayerList() {
        List<TestLayer> layers = testLayerRepository.findByIsActiveOrderByLayerOrder("1");
        return Result.success(layers);
    }
    
    @Operation(summary = "获取测试设计方法列表", description = "获取所有启用的测试设计方法列表")
    @GetMapping("/test-design-methods")
    public Result<List<TestDesignMethod>> getTestDesignMethodList() {
        List<TestDesignMethod> methods = testMethodRepository.findByIsActive("1");
        return Result.success(methods);
    }
}

