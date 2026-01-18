package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestExecutionTaskRequestDTO;
import com.sinosoft.testdesign.dto.TestExecutionTaskResponseDTO;
import com.sinosoft.testdesign.dto.TestExecutionRecordRequestDTO;
import com.sinosoft.testdesign.dto.TestExecutionRecordResponseDTO;
import com.sinosoft.testdesign.dto.TestExecutionStatisticsDTO;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestExecutionService;
import com.sinosoft.testdesign.service.UIScriptRepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 测试执行控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "测试执行", description = "测试执行任务和记录相关接口")
@RestController
@RequestMapping("/v1/test-execution")
@RequiredArgsConstructor
public class TestExecutionController {
    
    private final TestExecutionService testExecutionService;
    private final UIScriptRepairService uiScriptRepairService;
    private final EntityDTOMapper entityDTOMapper;
    
    // ========== 执行任务相关接口 ==========
    
    @Operation(summary = "创建执行任务", description = "创建新的测试执行任务")
    @PostMapping("/tasks")
    public Result<TestExecutionTaskResponseDTO> createExecutionTask(@Valid @RequestBody TestExecutionTaskRequestDTO dto) {
        TestExecutionTask task = entityDTOMapper.toTestExecutionTaskEntity(dto);
        TestExecutionTask saved = testExecutionService.createExecutionTask(task);
        return Result.success(entityDTOMapper.toTestExecutionTaskResponseDTO(saved));
    }
    
    @Operation(summary = "查询执行任务列表", description = "分页查询执行任务列表，支持按任务名称、状态、类型搜索")
    @GetMapping("/tasks")
    public Result<Page<TestExecutionTaskResponseDTO>> getExecutionTaskList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskStatus,
            @RequestParam(required = false) String taskType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestExecutionTask> taskPage = testExecutionService.getExecutionTaskList(pageable, taskName, taskStatus, taskType);
        
        Page<TestExecutionTaskResponseDTO> dtoPage = taskPage.map(entityDTOMapper::toTestExecutionTaskResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取执行任务详情", description = "根据ID获取执行任务详情")
    @GetMapping("/tasks/{id}")
    public Result<TestExecutionTaskResponseDTO> getExecutionTaskById(@PathVariable Long id) {
        TestExecutionTask task = testExecutionService.getExecutionTaskById(id);
        return Result.success(entityDTOMapper.toTestExecutionTaskResponseDTO(task));
    }
    
    @Operation(summary = "根据任务编码获取执行任务详情", description = "根据任务编码获取执行任务详情")
    @GetMapping("/tasks/code/{taskCode}")
    public Result<TestExecutionTaskResponseDTO> getExecutionTaskByCode(@PathVariable String taskCode) {
        TestExecutionTask task = testExecutionService.getExecutionTaskByCode(taskCode);
        return Result.success(entityDTOMapper.toTestExecutionTaskResponseDTO(task));
    }
    
    @Operation(summary = "更新执行任务", description = "更新执行任务信息")
    @PutMapping("/tasks/{id}")
    public Result<TestExecutionTaskResponseDTO> updateExecutionTask(
            @PathVariable Long id,
            @Valid @RequestBody TestExecutionTaskRequestDTO dto) {
        TestExecutionTask task = entityDTOMapper.toTestExecutionTaskEntity(dto);
        TestExecutionTask updated = testExecutionService.updateExecutionTask(id, task);
        return Result.success(entityDTOMapper.toTestExecutionTaskResponseDTO(updated));
    }
    
    @Operation(summary = "删除执行任务", description = "删除指定执行任务")
    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteExecutionTask(@PathVariable Long id) {
        testExecutionService.deleteExecutionTask(id);
        return Result.success();
    }
    
    @Operation(summary = "更新任务状态", description = "更新任务状态（状态流转）")
    @PutMapping("/tasks/{taskCode}/status")
    public Result<TestExecutionTaskResponseDTO> updateTaskStatus(
            @PathVariable String taskCode,
            @RequestParam String status) {
        TestExecutionTask updated = testExecutionService.updateTaskStatus(taskCode, status);
        return Result.success(entityDTOMapper.toTestExecutionTaskResponseDTO(updated));
    }
    
    @Operation(summary = "更新任务进度", description = "更新任务进度（0-100）")
    @PutMapping("/tasks/{taskCode}/progress")
    public Result<TestExecutionTaskResponseDTO> updateTaskProgress(
            @PathVariable String taskCode,
            @RequestParam Integer progress) {
        TestExecutionTask updated = testExecutionService.updateTaskProgress(taskCode, progress);
        return Result.success(entityDTOMapper.toTestExecutionTaskResponseDTO(updated));
    }
    
    // ========== 执行记录相关接口 ==========
    
    @Operation(summary = "创建执行记录", description = "创建新的测试执行记录")
    @PostMapping("/records")
    public Result<TestExecutionRecordResponseDTO> createExecutionRecord(@Valid @RequestBody TestExecutionRecordRequestDTO dto) {
        TestExecutionRecord record = entityDTOMapper.toTestExecutionRecordEntity(dto);
        TestExecutionRecord saved = testExecutionService.createExecutionRecord(record);
        return Result.success(entityDTOMapper.toTestExecutionRecordResponseDTO(saved));
    }
    
    @Operation(summary = "查询执行记录列表", description = "分页查询执行记录列表，支持按任务ID、用例ID、执行状态搜索")
    @GetMapping("/records")
    public Result<Page<TestExecutionRecordResponseDTO>> getExecutionRecordList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) String executionStatus) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestExecutionRecord> recordPage = testExecutionService.getExecutionRecordList(pageable, taskId, caseId, executionStatus);
        
        Page<TestExecutionRecordResponseDTO> dtoPage = recordPage.map(entityDTOMapper::toTestExecutionRecordResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取执行记录详情", description = "根据ID获取执行记录详情")
    @GetMapping("/records/{id}")
    public Result<TestExecutionRecordResponseDTO> getExecutionRecordById(@PathVariable Long id) {
        TestExecutionRecord record = testExecutionService.getExecutionRecordById(id);
        return Result.success(entityDTOMapper.toTestExecutionRecordResponseDTO(record));
    }
    
    @Operation(summary = "根据记录编码获取执行记录详情", description = "根据记录编码获取执行记录详情")
    @GetMapping("/records/code/{recordCode}")
    public Result<TestExecutionRecordResponseDTO> getExecutionRecordByCode(@PathVariable String recordCode) {
        TestExecutionRecord record = testExecutionService.getExecutionRecordByCode(recordCode);
        return Result.success(entityDTOMapper.toTestExecutionRecordResponseDTO(record));
    }
    
    @Operation(summary = "根据任务ID查询执行记录列表", description = "根据任务ID查询所有执行记录")
    @GetMapping("/tasks/{taskId}/records")
    public Result<List<TestExecutionRecordResponseDTO>> getExecutionRecordsByTaskId(@PathVariable Long taskId) {
        List<TestExecutionRecord> records = testExecutionService.getExecutionRecordsByTaskId(taskId);
        List<TestExecutionRecordResponseDTO> dtoList = records.stream()
                .map(entityDTOMapper::toTestExecutionRecordResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "更新执行记录状态", description = "更新执行记录状态（状态流转）")
    @PutMapping("/records/{recordCode}/status")
    public Result<TestExecutionRecordResponseDTO> updateExecutionRecordStatus(
            @PathVariable String recordCode,
            @RequestParam String status) {
        TestExecutionRecord updated = testExecutionService.updateExecutionRecordStatus(recordCode, status);
        return Result.success(entityDTOMapper.toTestExecutionRecordResponseDTO(updated));
    }
    
    // ========== UI脚本修复相关接口 ==========
    
    @Operation(summary = "分析错误日志", description = "分析UI脚本执行错误日志，识别错误类型和修复建议")
    @PostMapping("/ui-script/analyze-error")
    public Result<java.util.Map<String, Object>> analyzeError(
            @RequestBody java.util.Map<String, Object> request) {
        String errorLog = (String) request.get("errorLog");
        String scriptContent = (String) request.get("scriptContent");
        Boolean useLlm = (Boolean) request.getOrDefault("useLlm", true);
        
        if (errorLog == null || errorLog.isEmpty()) {
            return Result.fail("错误日志不能为空");
        }
        
        java.util.Map<String, Object> result = uiScriptRepairService.analyzeError(
                errorLog, 
                scriptContent, 
                useLlm != null ? useLlm : true
        );
        
        return Result.success(result);
    }
    
    @Operation(summary = "检测页面变化", description = "检测页面代码变化，识别元素定位信息变化")
    @PostMapping("/ui-script/detect-page-changes")
    public Result<java.util.Map<String, Object>> detectPageChanges(
            @RequestBody java.util.Map<String, Object> request) {
        String oldPageCodeUrl = (String) request.get("oldPageCodeUrl");
        String newPageCodeUrl = (String) request.get("newPageCodeUrl");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> oldPageElements = 
                (java.util.List<Map<String, Object>>) request.get("oldPageElements");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> newPageElements = 
                (java.util.List<Map<String, Object>>) request.get("newPageElements");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> scriptLocators = 
                (java.util.List<Map<String, Object>>) request.get("scriptLocators");
        
        java.util.Map<String, Object> result = uiScriptRepairService.detectPageChanges(
                oldPageCodeUrl,
                oldPageElements,
                newPageCodeUrl,
                newPageElements,
                scriptLocators
        );
        
        return Result.success(result);
    }
    
    @Operation(summary = "修复UI脚本", description = "修复执行失败的UI自动化脚本")
    @PostMapping("/ui-script/repair")
    public Result<java.util.Map<String, Object>> repairScript(
            @RequestBody java.util.Map<String, Object> request) {
        String scriptContent = (String) request.get("scriptContent");
        String errorLog = (String) request.get("errorLog");
        
        if (scriptContent == null || scriptContent.isEmpty()) {
            return Result.fail("脚本内容不能为空");
        }
        if (errorLog == null || errorLog.isEmpty()) {
            return Result.fail("错误日志不能为空");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> errorAnalysis = (Map<String, Object>) request.get("errorAnalysis");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pageChanges = (Map<String, Object>) request.get("pageChanges");
        
        String newPageCodeUrl = (String) request.get("newPageCodeUrl");
        
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> newPageElements = 
                (java.util.List<Map<String, Object>>) request.get("newPageElements");
        
        String scriptType = (String) request.getOrDefault("scriptType", "SELENIUM");
        String scriptLanguage = (String) request.getOrDefault("scriptLanguage", "PYTHON");
        Boolean useLlm = (Boolean) request.getOrDefault("useLlm", true);
        
        java.util.Map<String, Object> result = uiScriptRepairService.repairScript(
                scriptContent,
                errorLog,
                errorAnalysis,
                pageChanges,
                newPageCodeUrl,
                newPageElements,
                scriptType,
                scriptLanguage,
                useLlm != null ? useLlm : true
        );
        
        return Result.success(result);
    }
    
    // ========== 统计分析相关接口 ==========
    
    @Operation(summary = "获取执行统计信息", description = "获取测试执行统计信息，包括任务统计、执行记录统计和趋势统计")
    @GetMapping("/statistics")
    public Result<TestExecutionStatisticsDTO> getExecutionStatistics(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        TestExecutionStatisticsDTO statistics = testExecutionService.getExecutionStatistics(
                requirementId, caseId, startDate, endDate);
        return Result.success(statistics);
    }
}

