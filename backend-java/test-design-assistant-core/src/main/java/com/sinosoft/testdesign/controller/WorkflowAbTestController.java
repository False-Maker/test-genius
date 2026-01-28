package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.WorkflowAbTestRequestDTO;
import com.sinosoft.testdesign.dto.WorkflowAbTestResponseDTO;
import com.sinosoft.testdesign.service.WorkflowAbTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流 A/B 测试控制器（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Tag(name = "工作流 A/B 测试", description = "工作流 A/B 测试配置与统计")
@RestController
@RequestMapping("/v1/workflows/{workflowId}/ab-tests")
@RequiredArgsConstructor
public class WorkflowAbTestController {

    private final WorkflowAbTestService workflowAbTestService;

    @Operation(summary = "创建 A/B 测试", description = "为指定工作流创建 A/B 测试")
    @PostMapping
    public Result<WorkflowAbTestResponseDTO> createAbTest(
            @PathVariable Long workflowId,
            @Valid @RequestBody WorkflowAbTestRequestDTO dto) {
        WorkflowAbTestResponseDTO saved = workflowAbTestService.createAbTest(workflowId, dto);
        return Result.success(saved);
    }

    @Operation(summary = "A/B 测试列表", description = "查询指定工作流的所有 A/B 测试")
    @GetMapping
    public Result<List<WorkflowAbTestResponseDTO>> getAbTests(@PathVariable Long workflowId) {
        return Result.success(workflowAbTestService.getAbTestsByWorkflowId(workflowId));
    }

    @Operation(summary = "A/B 测试详情", description = "根据 ID 获取 A/B 测试详情")
    @GetMapping("/{id}")
    public Result<WorkflowAbTestResponseDTO> getAbTestById(@PathVariable Long id) {
        return Result.success(workflowAbTestService.getAbTestById(id));
    }

    @Operation(summary = "启动 A/B 测试", description = "启动指定的 A/B 测试")
    @PostMapping("/{id}/start")
    public Result<WorkflowAbTestResponseDTO> startAbTest(@PathVariable Long id) {
        return Result.success(workflowAbTestService.startAbTest(id));
    }

    @Operation(summary = "暂停 A/B 测试", description = "暂停指定的 A/B 测试")
    @PostMapping("/{id}/pause")
    public Result<WorkflowAbTestResponseDTO> pauseAbTest(@PathVariable Long id) {
        return Result.success(workflowAbTestService.pauseAbTest(id));
    }

    @Operation(summary = "停止 A/B 测试", description = "停止指定的 A/B 测试")
    @PostMapping("/{id}/stop")
    public Result<WorkflowAbTestResponseDTO> stopAbTest(@PathVariable Long id) {
        return Result.success(workflowAbTestService.stopAbTest(id));
    }

    @Operation(summary = "A/B 测试统计", description = "获取 A/B 测试的统计信息")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getAbTestStatistics(@PathVariable Long id) {
        return Result.success(workflowAbTestService.getAbTestStatistics(id));
    }

    @Operation(summary = "删除 A/B 测试", description = "删除指定的 A/B 测试")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAbTest(@PathVariable Long id) {
        workflowAbTestService.deleteAbTest(id);
        return Result.success();
    }
}
