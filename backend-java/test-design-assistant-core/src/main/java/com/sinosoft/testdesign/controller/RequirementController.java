package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestRequirementRequestDTO;
import com.sinosoft.testdesign.dto.TestRequirementResponseDTO;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.RequirementAnalysisService;
import com.sinosoft.testdesign.service.RequirementService;
import com.sinosoft.testdesign.service.impl.RequirementAnalysisServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

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
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建需求", description = "创建新的测试需求")
    @PostMapping
    public Result<TestRequirementResponseDTO> createRequirement(@Valid @RequestBody TestRequirementRequestDTO dto) {
        TestRequirement requirement = entityDTOMapper.toRequirementEntity(dto);
        TestRequirement saved = requirementService.createRequirement(requirement);
        return Result.success(entityDTOMapper.toRequirementResponseDTO(saved));
    }
    
    @Operation(summary = "查询需求列表", description = "分页查询需求列表，支持按需求名称和状态搜索")
    @GetMapping
    public Result<Page<TestRequirementResponseDTO>> getRequirementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String requirementName,
            @RequestParam(required = false) String requirementStatus) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestRequirement> requirementPage = requirementService.getRequirementList(pageable, requirementName, requirementStatus);
        
        // 转换为DTO分页
        Page<TestRequirementResponseDTO> dtoPage = requirementPage.map(entityDTOMapper::toRequirementResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取需求详情", description = "根据ID获取需求详情")
    @GetMapping("/{id}")
    public Result<TestRequirementResponseDTO> getRequirementById(@PathVariable Long id) {
        TestRequirement requirement = requirementService.getRequirementById(id);
        return Result.success(entityDTOMapper.toRequirementResponseDTO(requirement));
    }
    
    @Operation(summary = "更新需求", description = "更新需求信息")
    @PutMapping("/{id}")
    public Result<TestRequirementResponseDTO> updateRequirement(
            @PathVariable Long id,
            @Valid @RequestBody TestRequirementRequestDTO dto) {
        TestRequirement requirement = requirementService.getRequirementById(id);
        entityDTOMapper.updateRequirementFromDTO(dto, requirement);
        TestRequirement updated = requirementService.updateRequirement(id, requirement);
        return Result.success(entityDTOMapper.toRequirementResponseDTO(updated));
    }
    
    @Operation(summary = "删除需求", description = "删除指定需求")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRequirement(@PathVariable Long id) {
        requirementService.deleteRequirement(id);
        return Result.success();
    }
    
    @Operation(summary = "更新需求状态", description = "更新需求状态（状态流转）")
    @PutMapping("/{id}/status")
    public Result<TestRequirementResponseDTO> updateRequirementStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        TestRequirement updated = requirementService.updateRequirementStatus(id, status);
        return Result.success(entityDTOMapper.toRequirementResponseDTO(updated));
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

