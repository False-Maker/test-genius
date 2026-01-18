package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestReportRequestDTO;
import com.sinosoft.testdesign.dto.TestReportResponseDTO;
import com.sinosoft.testdesign.entity.TestReport;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试报告管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "测试报告管理", description = "测试报告管理相关接口")
@RestController
@RequestMapping("/v1/test-reports")
@RequiredArgsConstructor
public class TestReportController {
    
    private final TestReportService reportService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "生成测试报告", description = "生成新的测试报告")
    @PostMapping
    public Result<TestReportResponseDTO> generateReport(@Valid @RequestBody TestReportRequestDTO dto) {
        TestReport report = entityDTOMapper.toTestReportEntity(dto);
        TestReport generated = reportService.generateReport(report);
        return Result.success(entityDTOMapper.toTestReportResponseDTO(generated));
    }
    
    @Operation(summary = "查询报告列表", description = "分页查询测试报告列表")
    @GetMapping
    public Result<Page<TestReportResponseDTO>> getReportList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestReport> reportPage = reportService.getReportList(pageable);
        
        // 转换为DTO分页
        Page<TestReportResponseDTO> dtoPage = reportPage.map(entityDTOMapper::toTestReportResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取报告详情", description = "根据ID获取报告详情")
    @GetMapping("/{id}")
    public Result<TestReportResponseDTO> getReportById(@PathVariable Long id) {
        TestReport report = reportService.getReportById(id);
        return Result.success(entityDTOMapper.toTestReportResponseDTO(report));
    }
    
    @Operation(summary = "根据编码获取报告详情", description = "根据报告编码获取报告详情")
    @GetMapping("/code/{reportCode}")
    public Result<TestReportResponseDTO> getReportByCode(@PathVariable String reportCode) {
        TestReport report = reportService.getReportByCode(reportCode);
        return Result.success(entityDTOMapper.toTestReportResponseDTO(report));
    }
    
    @Operation(summary = "根据需求ID查询报告列表", description = "根据需求ID查询关联的测试报告列表")
    @GetMapping("/requirement/{requirementId}")
    public Result<List<TestReportResponseDTO>> getReportsByRequirementId(@PathVariable Long requirementId) {
        List<TestReport> reports = reportService.getReportsByRequirementId(requirementId);
        List<TestReportResponseDTO> dtoList = reports.stream()
                .map(entityDTOMapper::toTestReportResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据执行任务ID查询报告列表", description = "根据执行任务ID查询关联的测试报告列表")
    @GetMapping("/execution-task/{executionTaskId}")
    public Result<List<TestReportResponseDTO>> getReportsByExecutionTaskId(@PathVariable Long executionTaskId) {
        List<TestReport> reports = reportService.getReportsByExecutionTaskId(executionTaskId);
        List<TestReportResponseDTO> dtoList = reports.stream()
                .map(entityDTOMapper::toTestReportResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "更新报告", description = "更新测试报告")
    @PutMapping("/{id}")
    public Result<TestReportResponseDTO> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody TestReportRequestDTO dto) {
        TestReport report = reportService.getReportById(id);
        entityDTOMapper.updateTestReportFromDTO(dto, report);
        TestReport updated = reportService.updateReport(id, report);
        return Result.success(entityDTOMapper.toTestReportResponseDTO(updated));
    }
    
    @Operation(summary = "发布报告", description = "发布测试报告（状态变更为PUBLISHED）")
    @PutMapping("/{id}/publish")
    public Result<TestReportResponseDTO> publishReport(@PathVariable Long id) {
        TestReport report = reportService.publishReport(id);
        return Result.success(entityDTOMapper.toTestReportResponseDTO(report));
    }
    
    @Operation(summary = "删除报告", description = "删除指定报告（仅限草稿状态）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.success();
    }
    
    @Operation(summary = "导出报告文件", description = "导出测试报告为指定格式的文件（WORD/PDF/EXCEL）")
    @GetMapping("/{reportCode}/export")
    public Result<String> exportReport(
            @PathVariable String reportCode,
            @RequestParam String format) {
        String fileUrl = reportService.exportReport(reportCode, format);
        return Result.success(fileUrl);
    }
    
    @Operation(summary = "汇总测试执行结果", description = "汇总测试执行结果（JSON格式）")
    @GetMapping("/summarize")
    public Result<String> summarizeExecutionResults(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) Long executionTaskId) {
        String summary = reportService.summarizeExecutionResults(requirementId, executionTaskId);
        return Result.success(summary);
    }
}

