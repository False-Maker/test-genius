package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.service.RequirementAnalysisService;
import com.sinosoft.testdesign.service.RequirementService;
import com.sinosoft.testdesign.service.impl.RequirementAnalysisServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 需求管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "需求管理", description = "需求管理相关接口")
@RestController
@RequestMapping("/v1/requirements")
@RequiredArgsConstructor
public class RequirementController {
    
    private final RequirementService requirementService;
    private final RequirementAnalysisService requirementAnalysisService;
    
    @Operation(summary = "创建需求", description = "创建新的测试需求")
    @PostMapping
    public Result<TestRequirement> createRequirement(@RequestBody TestRequirement requirement) {
        return Result.success(requirementService.createRequirement(requirement));
    }
    
    @Operation(summary = "查询需求列表", description = "分页查询需求列表，支持按需求名称和状态搜索")
    @GetMapping
    public Result<Page<TestRequirement>> getRequirementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String requirementName,
            @RequestParam(required = false) String requirementStatus) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(requirementService.getRequirementList(pageable, requirementName, requirementStatus));
    }
    
    @Operation(summary = "获取需求详情", description = "根据ID获取需求详情")
    @GetMapping("/{id}")
    public Result<TestRequirement> getRequirementById(@PathVariable Long id) {
        return Result.success(requirementService.getRequirementById(id));
    }
    
    @Operation(summary = "更新需求", description = "更新需求信息")
    @PutMapping("/{id}")
    public Result<TestRequirement> updateRequirement(
            @PathVariable Long id,
            @RequestBody TestRequirement requirement) {
        return Result.success(requirementService.updateRequirement(id, requirement));
    }
    
    @Operation(summary = "删除需求", description = "删除指定需求")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRequirement(@PathVariable Long id) {
        requirementService.deleteRequirement(id);
        return Result.success();
    }
    
    @Operation(summary = "更新需求状态", description = "更新需求状态（状态流转）")
    @PutMapping("/{id}/status")
    public Result<TestRequirement> updateRequirementStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return Result.success(requirementService.updateRequirementStatus(id, status));
    }
    
    @Operation(summary = "分析需求", description = "分析需求文档，提取测试要点和业务规则")
    @PostMapping("/{id}/analyze")
    public Result<RequirementAnalysisServiceImpl.RequirementAnalysisResult> analyzeRequirement(@PathVariable Long id) {
        RequirementAnalysisServiceImpl.RequirementAnalysisResult result = 
            requirementAnalysisService.analyzeRequirement(id);
        return Result.success(result);
    }
    
    @Operation(summary = "提取测试要点", description = "从需求中提取测试要点")
    @GetMapping("/{id}/test-points")
    public Result<java.util.List<RequirementAnalysisServiceImpl.TestPoint>> getTestPoints(@PathVariable Long id) {
        java.util.List<RequirementAnalysisServiceImpl.TestPoint> testPoints = 
            requirementAnalysisService.extractTestPoints(id);
        return Result.success(testPoints);
    }
    
    @Operation(summary = "提取业务规则", description = "从需求中提取业务规则")
    @GetMapping("/{id}/business-rules")
    public Result<java.util.List<RequirementAnalysisServiceImpl.BusinessRule>> getBusinessRules(@PathVariable Long id) {
        java.util.List<RequirementAnalysisServiceImpl.BusinessRule> businessRules = 
            requirementAnalysisService.extractBusinessRules(id);
        return Result.success(businessRules);
    }
}

