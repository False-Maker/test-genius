package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.WorkflowExecution;
import com.sinosoft.testdesign.service.WorkflowExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流执行控制器
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Tag(name = "工作流执行", description = "工作流执行管理相关接口")
@RestController
@RequestMapping("/v1/workflow-executions")
@RequiredArgsConstructor
public class WorkflowExecutionController {
    
    private final WorkflowExecutionService executionService;
    
    @Operation(summary = "执行工作流", description = "执行指定的工作流")
    @PostMapping("/execute")
    public Result<WorkflowExecution> executeWorkflow(
            @RequestParam Long workflowId,
            @RequestBody Map<String, Object> inputData,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String creatorName) {
        WorkflowExecution execution = executionService.executeWorkflow(
                workflowId, inputData, creatorId, creatorName);
        return Result.success(execution);
    }
    
    @Operation(summary = "查询执行记录", description = "根据执行ID查询执行记录")
    @GetMapping("/{executionId}")
    public Result<WorkflowExecution> getExecution(@PathVariable String executionId) {
        Optional<WorkflowExecution> execution = executionService.findByExecutionId(executionId);
        return execution.map(Result::success)
                .orElse(Result.error("执行记录不存在"));
    }
    
    @Operation(summary = "根据工作流ID查询执行记录", description = "根据工作流ID查询执行记录列表")
    @GetMapping("/workflow/{workflowId}")
    public Result<List<WorkflowExecution>> getExecutionsByWorkflowId(@PathVariable Long workflowId) {
        List<WorkflowExecution> executions = executionService.findByWorkflowId(workflowId);
        return Result.success(executions);
    }
    
    @Operation(summary = "根据工作流代码查询执行记录", description = "根据工作流代码查询执行记录列表")
    @GetMapping("/workflow-code/{workflowCode}")
    public Result<List<WorkflowExecution>> getExecutionsByWorkflowCode(@PathVariable String workflowCode) {
        List<WorkflowExecution> executions = executionService.findByWorkflowCode(workflowCode);
        return Result.success(executions);
    }
    
    @Operation(summary = "根据状态查询执行记录", description = "根据状态查询执行记录列表")
    @GetMapping("/status/{status}")
    public Result<List<WorkflowExecution>> getExecutionsByStatus(@PathVariable String status) {
        List<WorkflowExecution> executions = executionService.findByStatus(status);
        return Result.success(executions);
    }
    
    @Operation(summary = "分页查询执行记录", description = "分页查询执行记录列表")
    @GetMapping
    public Result<Page<WorkflowExecution>> getExecutionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkflowExecution> executions = executionService.findAll(pageable);
        return Result.success(executions);
    }
    
    @Operation(summary = "取消执行", description = "取消正在执行的工作流")
    @PostMapping("/{executionId}/cancel")
    public Result<WorkflowExecution> cancelExecution(@PathVariable String executionId) {
        WorkflowExecution execution = executionService.cancelExecution(executionId);
        return Result.success(execution);
    }
    
    @Operation(summary = "查询执行进度", description = "查询工作流执行进度")
    @GetMapping("/{executionId}/progress")
    public Result<Map<String, Object>> getExecutionProgress(@PathVariable String executionId) {
        Map<String, Object> progress = executionService.getExecutionProgress(executionId);
        return Result.success(progress);
    }
}
