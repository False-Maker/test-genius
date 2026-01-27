package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.PromptTemplateAbTestRequestDTO;
import com.sinosoft.testdesign.dto.PromptTemplateAbTestResponseDTO;
import com.sinosoft.testdesign.dto.PromptTemplateVersionResponseDTO;
import com.sinosoft.testdesign.entity.PromptTemplateAbTest;
import com.sinosoft.testdesign.entity.PromptTemplateVersion;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.PromptTemplateAbTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板A/B测试控制器
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Tag(name = "提示词模板A/B测试", description = "提示词模板A/B测试相关接口")
@RestController
@RequestMapping("/v1/prompt-templates/{templateId}/ab-tests")
@RequiredArgsConstructor
public class PromptTemplateAbTestController {
    
    private final PromptTemplateAbTestService abTestService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建A/B测试", description = "为指定模板创建A/B测试")
    @PostMapping
    public Result<PromptTemplateAbTestResponseDTO> createAbTest(
            @PathVariable Long templateId,
            @Valid @RequestBody PromptTemplateAbTestRequestDTO dto) {
        PromptTemplateAbTest abTest = entityDTOMapper.toPromptTemplateAbTestEntity(dto);
        PromptTemplateAbTest saved = abTestService.createAbTest(templateId, abTest);
        return Result.success(entityDTOMapper.toPromptTemplateAbTestResponseDTO(saved));
    }
    
    @Operation(summary = "查询A/B测试列表", description = "查询指定模板的所有A/B测试")
    @GetMapping
    public Result<List<PromptTemplateAbTestResponseDTO>> getAbTests(@PathVariable Long templateId) {
        List<PromptTemplateAbTest> abTests = abTestService.getAbTestsByTemplateId(templateId);
        List<PromptTemplateAbTestResponseDTO> dtoList = entityDTOMapper.toPromptTemplateAbTestResponseDTOList(abTests);
        return Result.success(dtoList);
    }
    
    @Operation(summary = "获取A/B测试详情", description = "根据ID获取A/B测试详情")
    @GetMapping("/{id}")
    public Result<PromptTemplateAbTestResponseDTO> getAbTestById(@PathVariable Long id) {
        PromptTemplateAbTest abTest = abTestService.getAbTestById(id);
        return Result.success(entityDTOMapper.toPromptTemplateAbTestResponseDTO(abTest));
    }
    
    @Operation(summary = "启动A/B测试", description = "启动指定的A/B测试")
    @PostMapping("/{id}/start")
    public Result<PromptTemplateAbTestResponseDTO> startAbTest(@PathVariable Long id) {
        PromptTemplateAbTest abTest = abTestService.startAbTest(id);
        return Result.success(entityDTOMapper.toPromptTemplateAbTestResponseDTO(abTest));
    }
    
    @Operation(summary = "暂停A/B测试", description = "暂停指定的A/B测试")
    @PostMapping("/{id}/pause")
    public Result<PromptTemplateAbTestResponseDTO> pauseAbTest(@PathVariable Long id) {
        PromptTemplateAbTest abTest = abTestService.pauseAbTest(id);
        return Result.success(entityDTOMapper.toPromptTemplateAbTestResponseDTO(abTest));
    }
    
    @Operation(summary = "停止A/B测试", description = "停止指定的A/B测试")
    @PostMapping("/{id}/stop")
    public Result<PromptTemplateAbTestResponseDTO> stopAbTest(@PathVariable Long id) {
        PromptTemplateAbTest abTest = abTestService.stopAbTest(id);
        return Result.success(entityDTOMapper.toPromptTemplateAbTestResponseDTO(abTest));
    }
    
    @Operation(summary = "获取A/B测试统计", description = "获取A/B测试的统计信息")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getAbTestStatistics(@PathVariable Long id) {
        Map<String, Object> stats = abTestService.getAbTestStatistics(id);
        return Result.success(stats);
    }
    
    @Operation(summary = "自动选择最优版本", description = "基于效果指标自动选择最优版本")
    @PostMapping("/{id}/auto-select")
    public Result<PromptTemplateVersionResponseDTO> autoSelectBestVersion(@PathVariable Long id) {
        PromptTemplateVersion bestVersion = abTestService.autoSelectBestVersion(id);
        return Result.success(entityDTOMapper.toPromptTemplateVersionResponseDTO(bestVersion));
    }
    
    @Operation(summary = "删除A/B测试", description = "删除指定的A/B测试")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAbTest(@PathVariable Long id) {
        abTestService.deleteAbTest(id);
        return Result.success();
    }
}
