package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.service.IntelligentCaseGenerationService;
import com.sinosoft.testdesign.util.CaseExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Operation(summary = "查询任务列表", description = "分页查询用例生成任务列表")
    @PostMapping("/tasks/list")
    public Result<PageResult<TaskListDTO>> getTaskList(@RequestBody TaskListQueryDTO query) {
        PageResult<TaskListDTO> result = caseGenerationService.getTaskList(query);
        return Result.success(result);
    }

    @Operation(summary = "查询任务详情", description = "查询用例生成任务详情，包含生成的用例列表")
    @GetMapping("/tasks/{taskId}")
    public Result<TaskDetailDTO> getTaskDetail(@PathVariable Long taskId) {
        TaskDetailDTO detail = caseGenerationService.getTaskDetail(taskId);
        return Result.success(detail);
    }

    @Operation(summary = "导出任务用例到Excel", description = "将指定任务生成的用例导出为Excel文件")
    @GetMapping("/tasks/{taskId}/export-excel")
    public void exportTaskToExcel(@PathVariable Long taskId, HttpServletResponse response) throws IOException {
        // 查询任务详情
        TaskDetailDTO taskDetail = caseGenerationService.getTaskDetail(taskId);

        // 生成Excel
        byte[] excelData = CaseExportUtil.exportTaskToExcel(taskDetail);

        // 设置响应头
        String fileName = URLEncoder.encode(
                "用例生成任务_" + taskDetail.getTaskCode() + ".xlsx",
                StandardCharsets.UTF_8
        );
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"");
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(excelData.length));

        // 写入响应
        response.getOutputStream().write(excelData);
        response.getOutputStream().flush();
    }
}

