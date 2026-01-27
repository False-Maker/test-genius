package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.WorkflowDefinition;
import com.sinosoft.testdesign.entity.WorkflowVersion;
import com.sinosoft.testdesign.service.WorkflowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流定义控制器
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Tag(name = "工作流定义", description = "工作流定义管理相关接口")
@RestController
@RequestMapping("/v1/workflows")
@RequiredArgsConstructor
public class WorkflowDefinitionController {
    
    private final WorkflowDefinitionService workflowService;
    
    @Operation(summary = "创建工作流定义", description = "创建新的工作流定义")
    @PostMapping
    public Result<WorkflowDefinition> createWorkflow(@Valid @RequestBody WorkflowDefinition workflow) {
        WorkflowDefinition saved = workflowService.createWorkflow(workflow);
        return Result.success(saved);
    }
    
    @Operation(summary = "更新工作流定义", description = "更新工作流定义")
    @PutMapping("/{id}")
    public Result<WorkflowDefinition> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowDefinition workflow) {
        workflow.setId(id);
        WorkflowDefinition updated = workflowService.updateWorkflow(workflow);
        return Result.success(updated);
    }
    
    @Operation(summary = "查询工作流定义", description = "根据ID查询工作流定义")
    @GetMapping("/{id}")
    public Result<WorkflowDefinition> getWorkflow(@PathVariable Long id) {
        Optional<WorkflowDefinition> workflow = workflowService.findById(id);
        return workflow.map(Result::success)
                .orElse(Result.error("工作流定义不存在"));
    }
    
    @Operation(summary = "根据代码查询工作流定义", description = "根据工作流代码查询工作流定义")
    @GetMapping("/code/{workflowCode}")
    public Result<WorkflowDefinition> getWorkflowByCode(@PathVariable String workflowCode) {
        Optional<WorkflowDefinition> workflow = workflowService.findByCode(workflowCode);
        return workflow.map(Result::success)
                .orElse(Result.error("工作流定义不存在"));
    }
    
    @Operation(summary = "查询所有工作流定义", description = "查询所有工作流定义列表")
    @GetMapping
    public Result<List<WorkflowDefinition>> getAllWorkflows() {
        List<WorkflowDefinition> workflows = workflowService.findAll();
        return Result.success(workflows);
    }
    
    @Operation(summary = "根据类型查询工作流定义", description = "根据工作流类型查询工作流定义列表")
    @GetMapping("/type/{workflowType}")
    public Result<List<WorkflowDefinition>> getWorkflowsByType(@PathVariable String workflowType) {
        List<WorkflowDefinition> workflows = workflowService.findByType(workflowType);
        return Result.success(workflows);
    }
    
    @Operation(summary = "查询所有启用的工作流定义", description = "查询所有启用的工作流定义列表")
    @GetMapping("/active")
    public Result<List<WorkflowDefinition>> getActiveWorkflows() {
        List<WorkflowDefinition> workflows = workflowService.findAllActive();
        return Result.success(workflows);
    }
    
    @Operation(summary = "删除工作流定义", description = "根据ID删除工作流定义")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteById(id);
        return Result.success();
    }
    
    @Operation(summary = "启用/禁用工作流", description = "启用或禁用工作流")
    @PutMapping("/{id}/toggle-active")
    public Result<WorkflowDefinition> toggleActive(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        WorkflowDefinition workflow = workflowService.toggleActive(id, isActive);
        return Result.success(workflow);
    }
    
    @Operation(summary = "设置默认工作流", description = "设置或取消默认工作流")
    @PutMapping("/{id}/set-default")
    public Result<WorkflowDefinition> setDefault(
            @PathVariable Long id,
            @RequestParam Boolean isDefault) {
        WorkflowDefinition workflow = workflowService.setDefault(id, isDefault);
        return Result.success(workflow);
    }
    
    @Operation(summary = "验证工作流配置", description = "验证工作流配置的合法性")
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateWorkflowConfig(@RequestBody Map<String, String> request) {
        String workflowConfig = request.get("workflowConfig");
        if (workflowConfig == null) {
            return Result.error("工作流配置不能为空");
        }
        Map<String, Object> result = workflowService.validateWorkflowConfig(workflowConfig);
        return Result.success(result);
    }
    
    @Operation(summary = "创建工作流版本", description = "为工作流创建新版本")
    @PostMapping("/{id}/versions")
    public Result<WorkflowVersion> createVersion(
            @PathVariable Long id,
            @RequestParam(required = false) String versionDescription) {
        WorkflowVersion version = workflowService.createVersion(id, versionDescription);
        return Result.success(version);
    }
    
    @Operation(summary = "查询工作流版本列表", description = "查询工作流的所有版本")
    @GetMapping("/{id}/versions")
    public Result<List<WorkflowVersion>> getVersions(@PathVariable Long id) {
        List<WorkflowVersion> versions = workflowService.findVersionsByWorkflowId(id);
        return Result.success(versions);
    }
    
    @Operation(summary = "回滚到指定版本", description = "将工作流回滚到指定版本")
    @PostMapping("/{id}/rollback")
    public Result<WorkflowDefinition> rollbackToVersion(
            @PathVariable Long id,
            @RequestParam Integer version) {
        WorkflowDefinition workflow = workflowService.rollbackToVersion(id, version);
        return Result.success(workflow);
    }
}
