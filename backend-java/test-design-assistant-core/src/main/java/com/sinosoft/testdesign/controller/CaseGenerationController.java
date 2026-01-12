package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.BatchCaseGenerationRequest;
import com.sinosoft.testdesign.dto.BatchCaseGenerationResult;
import com.sinosoft.testdesign.dto.CaseGenerationRequest;
import com.sinosoft.testdesign.dto.CaseGenerationResult;
import com.sinosoft.testdesign.dto.GenerationTaskDTO;
import com.sinosoft.testdesign.service.IntelligentCaseGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用例生成控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "用例生成", description = "智能用例生成相关接口")
@RestController
@RequestMapping("/v1/case-generation")
@RequiredArgsConstructor
public class CaseGenerationController {
    
    private final IntelligentCaseGenerationService caseGenerationService;
    
    @Operation(summary = "生成用例", description = "根据需求生成测试用例（异步）")
    @PostMapping("/generate")
    public Result<CaseGenerationResult> generateTestCases(@RequestBody CaseGenerationRequest request) {
        CaseGenerationResult result = caseGenerationService.generateTestCases(request);
        return Result.success(result);
    }
    
    @Operation(summary = "批量生成用例", description = "根据多个需求批量生成测试用例（异步）")
    @PostMapping("/batch-generate")
    public Result<BatchCaseGenerationResult> batchGenerateTestCases(@RequestBody BatchCaseGenerationRequest request) {
        BatchCaseGenerationResult result = caseGenerationService.batchGenerateTestCases(request);
        return Result.success(result);
    }
    
    @Operation(summary = "查询生成任务", description = "查询用例生成任务状态")
    @GetMapping("/{id}")
    public Result<GenerationTaskDTO> getGenerationTask(@PathVariable Long id) {
        GenerationTaskDTO task = caseGenerationService.getGenerationTask(id);
        return Result.success(task);
    }
    
    @Operation(summary = "批量查询生成任务", description = "批量查询用例生成任务状态")
    @PostMapping("/batch-query")
    public Result<List<GenerationTaskDTO>> getBatchGenerationTasks(@RequestBody List<Long> taskIds) {
        List<GenerationTaskDTO> tasks = caseGenerationService.getBatchGenerationTasks(taskIds);
        return Result.success(tasks);
    }
}

