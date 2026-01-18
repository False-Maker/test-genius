package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestCoverageAnalysisRequestDTO;
import com.sinosoft.testdesign.dto.TestCoverageAnalysisResponseDTO;
import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestCoverageService;
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
 * 测试覆盖分析控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "测试覆盖分析", description = "测试覆盖分析相关接口")
@RestController
@RequestMapping("/v1/test-coverage")
@RequiredArgsConstructor
public class TestCoverageController {
    
    private final TestCoverageService coverageService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "分析测试覆盖", description = "执行测试覆盖分析（通用方法）")
    @PostMapping("/analyze")
    public Result<TestCoverageAnalysisResponseDTO> analyzeCoverage(
            @Valid @RequestBody TestCoverageAnalysisRequestDTO dto) {
        TestCoverageAnalysis analysis = entityDTOMapper.toTestCoverageAnalysisEntity(dto);
        TestCoverageAnalysis result = coverageService.analyzeCoverage(analysis);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(result));
    }
    
    @Operation(summary = "分析需求覆盖", description = "分析指定需求的覆盖情况")
    @PostMapping("/analyze/requirement/{requirementId}")
    public Result<TestCoverageAnalysisResponseDTO> analyzeRequirementCoverage(
            @PathVariable Long requirementId) {
        TestCoverageAnalysis result = coverageService.analyzeRequirementCoverage(requirementId);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(result));
    }
    
    @Operation(summary = "分析功能覆盖", description = "分析指定需求的功能覆盖情况")
    @PostMapping("/analyze/function/{requirementId}")
    public Result<TestCoverageAnalysisResponseDTO> analyzeFunctionCoverage(
            @PathVariable Long requirementId) {
        TestCoverageAnalysis result = coverageService.analyzeFunctionCoverage(requirementId);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(result));
    }
    
    @Operation(summary = "分析场景覆盖", description = "分析指定需求的场景覆盖情况")
    @PostMapping("/analyze/scenario/{requirementId}")
    public Result<TestCoverageAnalysisResponseDTO> analyzeScenarioCoverage(
            @PathVariable Long requirementId) {
        TestCoverageAnalysis result = coverageService.analyzeScenarioCoverage(requirementId);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(result));
    }
    
    @Operation(summary = "分析代码覆盖", description = "分析代码覆盖情况（需要提供覆盖数据）")
    @PostMapping("/analyze/code")
    public Result<TestCoverageAnalysisResponseDTO> analyzeCodeCoverage(
            @RequestParam(required = false) Long requirementId,
            @RequestBody String coverageData) {
        TestCoverageAnalysis result = coverageService.analyzeCodeCoverage(requirementId, coverageData);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(result));
    }
    
    @Operation(summary = "查询覆盖分析列表", description = "分页查询测试覆盖分析列表")
    @GetMapping
    public Result<Page<TestCoverageAnalysisResponseDTO>> getAnalysisList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestCoverageAnalysis> analysisPage = coverageService.getAnalysisList(pageable);
        
        // 转换为DTO分页
        Page<TestCoverageAnalysisResponseDTO> dtoPage = analysisPage.map(entityDTOMapper::toTestCoverageAnalysisResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取分析详情", description = "根据ID获取覆盖分析详情")
    @GetMapping("/{id}")
    public Result<TestCoverageAnalysisResponseDTO> getAnalysisById(@PathVariable Long id) {
        TestCoverageAnalysis analysis = coverageService.getAnalysisById(id);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(analysis));
    }
    
    @Operation(summary = "根据编码获取分析详情", description = "根据分析编码获取覆盖分析详情")
    @GetMapping("/code/{analysisCode}")
    public Result<TestCoverageAnalysisResponseDTO> getAnalysisByCode(@PathVariable String analysisCode) {
        TestCoverageAnalysis analysis = coverageService.getAnalysisByCode(analysisCode);
        return Result.success(entityDTOMapper.toTestCoverageAnalysisResponseDTO(analysis));
    }
    
    @Operation(summary = "根据需求ID查询分析列表", description = "根据需求ID查询关联的覆盖分析列表")
    @GetMapping("/requirement/{requirementId}")
    public Result<List<TestCoverageAnalysisResponseDTO>> getAnalysisByRequirementId(
            @PathVariable Long requirementId) {
        List<TestCoverageAnalysis> analyses = coverageService.getAnalysisByRequirementId(requirementId);
        List<TestCoverageAnalysisResponseDTO> dtoList = analyses.stream()
                .map(entityDTOMapper::toTestCoverageAnalysisResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据覆盖类型查询分析列表", description = "根据覆盖类型查询覆盖分析列表")
    @GetMapping("/type/{coverageType}")
    public Result<List<TestCoverageAnalysisResponseDTO>> getAnalysisByCoverageType(
            @PathVariable String coverageType) {
        List<TestCoverageAnalysis> analyses = coverageService.getAnalysisByCoverageType(coverageType);
        List<TestCoverageAnalysisResponseDTO> dtoList = analyses.stream()
                .map(entityDTOMapper::toTestCoverageAnalysisResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "获取覆盖趋势", description = "获取测试覆盖趋势分析数据")
    @GetMapping("/trend")
    public Result<String> getCoverageTrend(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) String coverageType,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        String trendData = coverageService.getCoverageTrend(requirementId, coverageType, days);
        return Result.success(trendData);
    }
    
    @Operation(summary = "检查覆盖不足", description = "检查覆盖率低于阈值的项")
    @GetMapping("/insufficiency")
    public Result<String> checkCoverageInsufficiency(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false, defaultValue = "80.0") Double threshold) {
        String result = coverageService.checkCoverageInsufficiency(requirementId, threshold);
        return Result.success(result);
    }
    
    @Operation(summary = "生成覆盖报告", description = "生成测试覆盖分析报告")
    @GetMapping("/report")
    public Result<String> generateCoverageReport(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) String coverageType) {
        String report = coverageService.generateCoverageReport(requirementId, coverageType);
        return Result.success(report);
    }
}

