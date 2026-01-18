package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestRiskAssessmentRequestDTO;
import com.sinosoft.testdesign.dto.TestRiskAssessmentResponseDTO;
import com.sinosoft.testdesign.entity.TestRiskAssessment;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestRiskAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 风险评估控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "风险评估", description = "风险评估相关接口")
@RestController
@RequestMapping("/v1/test-risk-assessment")
@RequiredArgsConstructor
public class TestRiskAssessmentController {
    
    private final TestRiskAssessmentService riskService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "执行风险评估", description = "执行风险评估（通用方法）")
    @PostMapping("/assess")
    public Result<TestRiskAssessmentResponseDTO> assessRisk(
            @Valid @RequestBody TestRiskAssessmentRequestDTO dto) {
        TestRiskAssessment assessment = entityDTOMapper.toTestRiskAssessmentEntity(dto);
        TestRiskAssessment result = riskService.assessRisk(assessment);
        return Result.success(entityDTOMapper.toTestRiskAssessmentResponseDTO(result));
    }
    
    @Operation(summary = "评估需求风险", description = "评估指定需求的风险")
    @PostMapping("/assess/requirement/{requirementId}")
    public Result<TestRiskAssessmentResponseDTO> assessRequirementRisk(
            @PathVariable Long requirementId) {
        TestRiskAssessment result = riskService.assessRequirementRisk(requirementId);
        return Result.success(entityDTOMapper.toTestRiskAssessmentResponseDTO(result));
    }
    
    @Operation(summary = "评估执行任务风险", description = "评估指定执行任务的风险")
    @PostMapping("/assess/execution-task/{executionTaskId}")
    public Result<TestRiskAssessmentResponseDTO> assessExecutionTaskRisk(
            @PathVariable Long executionTaskId) {
        TestRiskAssessment result = riskService.assessExecutionTaskRisk(executionTaskId);
        return Result.success(entityDTOMapper.toTestRiskAssessmentResponseDTO(result));
    }
    
    @Operation(summary = "评估风险等级", description = "根据风险评分评估风险等级")
    @GetMapping("/assess/level")
    public Result<String> assessRiskLevel(@RequestParam BigDecimal riskScore) {
        String riskLevel = riskService.assessRiskLevel(riskScore);
        return Result.success(riskLevel);
    }
    
    @Operation(summary = "评估上线可行性", description = "评估上线可行性评分")
    @GetMapping("/assess/feasibility")
    public Result<BigDecimal> assessFeasibility(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) Long executionTaskId) {
        BigDecimal feasibilityScore = riskService.assessFeasibility(requirementId, executionTaskId);
        return Result.success(feasibilityScore);
    }
    
    @Operation(summary = "识别风险项", description = "识别潜在的风险项")
    @GetMapping("/identify/risk-items")
    public Result<String> identifyRiskItems(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) Long executionTaskId) {
        String riskItems = riskService.identifyRiskItems(requirementId, executionTaskId);
        return Result.success(riskItems);
    }
    
    @Operation(summary = "查询风险评估列表", description = "分页查询风险评估列表")
    @GetMapping
    public Result<Page<TestRiskAssessmentResponseDTO>> getAssessmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestRiskAssessment> assessmentPage = riskService.getAssessmentList(pageable);
        
        // 转换为DTO分页
        Page<TestRiskAssessmentResponseDTO> dtoPage = assessmentPage.map(entityDTOMapper::toTestRiskAssessmentResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取评估详情", description = "根据ID获取风险评估详情")
    @GetMapping("/{id}")
    public Result<TestRiskAssessmentResponseDTO> getAssessmentById(@PathVariable Long id) {
        TestRiskAssessment assessment = riskService.getAssessmentById(id);
        return Result.success(entityDTOMapper.toTestRiskAssessmentResponseDTO(assessment));
    }
    
    @Operation(summary = "根据编码获取评估详情", description = "根据评估编码获取风险评估详情")
    @GetMapping("/code/{assessmentCode}")
    public Result<TestRiskAssessmentResponseDTO> getAssessmentByCode(@PathVariable String assessmentCode) {
        TestRiskAssessment assessment = riskService.getAssessmentByCode(assessmentCode);
        return Result.success(entityDTOMapper.toTestRiskAssessmentResponseDTO(assessment));
    }
    
    @Operation(summary = "根据需求ID查询评估列表", description = "根据需求ID查询关联的风险评估列表")
    @GetMapping("/requirement/{requirementId}")
    public Result<List<TestRiskAssessmentResponseDTO>> getAssessmentByRequirementId(
            @PathVariable Long requirementId) {
        List<TestRiskAssessment> assessments = riskService.getAssessmentByRequirementId(requirementId);
        List<TestRiskAssessmentResponseDTO> dtoList = assessments.stream()
                .map(entityDTOMapper::toTestRiskAssessmentResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据执行任务ID查询评估列表", description = "根据执行任务ID查询关联的风险评估列表")
    @GetMapping("/execution-task/{executionTaskId}")
    public Result<List<TestRiskAssessmentResponseDTO>> getAssessmentByExecutionTaskId(
            @PathVariable Long executionTaskId) {
        List<TestRiskAssessment> assessments = riskService.getAssessmentByExecutionTaskId(executionTaskId);
        List<TestRiskAssessmentResponseDTO> dtoList = assessments.stream()
                .map(entityDTOMapper::toTestRiskAssessmentResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据风险等级查询评估列表", description = "根据风险等级查询风险评估列表")
    @GetMapping("/level/{riskLevel}")
    public Result<List<TestRiskAssessmentResponseDTO>> getAssessmentByRiskLevel(
            @PathVariable String riskLevel) {
        List<TestRiskAssessment> assessments = riskService.getAssessmentByRiskLevel(riskLevel);
        List<TestRiskAssessmentResponseDTO> dtoList = assessments.stream()
                .map(entityDTOMapper::toTestRiskAssessmentResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
}

