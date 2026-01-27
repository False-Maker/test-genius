package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.PromptTemplateVersionRequestDTO;
import com.sinosoft.testdesign.dto.PromptTemplateVersionResponseDTO;
import com.sinosoft.testdesign.entity.PromptTemplateVersion;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.PromptTemplateVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板版本管理控制器
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Tag(name = "提示词模板版本管理", description = "提示词模板版本管理相关接口")
@RestController
@RequestMapping("/v1/prompt-templates/{templateId}/versions")
@RequiredArgsConstructor
public class PromptTemplateVersionController {
    
    private final PromptTemplateVersionService versionService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建版本", description = "为指定模板创建新版本")
    @PostMapping
    public Result<PromptTemplateVersionResponseDTO> createVersion(
            @PathVariable Long templateId,
            @Valid @RequestBody PromptTemplateVersionRequestDTO dto) {
        PromptTemplateVersion version = entityDTOMapper.toPromptTemplateVersionEntity(dto);
        PromptTemplateVersion saved = versionService.createVersion(templateId, version);
        return Result.success(entityDTOMapper.toPromptTemplateVersionResponseDTO(saved));
    }
    
    @Operation(summary = "查询版本列表", description = "查询指定模板的所有版本")
    @GetMapping
    public Result<List<PromptTemplateVersionResponseDTO>> getVersions(@PathVariable Long templateId) {
        List<PromptTemplateVersion> versions = versionService.getVersionsByTemplateId(templateId);
        List<PromptTemplateVersionResponseDTO> dtoList = entityDTOMapper.toPromptTemplateVersionResponseDTOList(versions);
        return Result.success(dtoList);
    }
    
    @Operation(summary = "获取版本详情", description = "根据ID获取版本详情")
    @GetMapping("/{id}")
    public Result<PromptTemplateVersionResponseDTO> getVersionById(@PathVariable Long id) {
        PromptTemplateVersion version = versionService.getVersionById(id);
        return Result.success(entityDTOMapper.toPromptTemplateVersionResponseDTO(version));
    }
    
    @Operation(summary = "获取当前版本", description = "获取指定模板的当前版本")
    @GetMapping("/current")
    public Result<PromptTemplateVersionResponseDTO> getCurrentVersion(@PathVariable Long templateId) {
        PromptTemplateVersion version = versionService.getCurrentVersion(templateId);
        return Result.success(entityDTOMapper.toPromptTemplateVersionResponseDTO(version));
    }
    
    @Operation(summary = "获取指定版本", description = "根据版本号获取指定版本")
    @GetMapping("/version/{versionNumber}")
    public Result<PromptTemplateVersionResponseDTO> getVersionByNumber(
            @PathVariable Long templateId,
            @PathVariable Integer versionNumber) {
        PromptTemplateVersion version = versionService.getVersionByTemplateIdAndVersionNumber(templateId, versionNumber);
        return Result.success(entityDTOMapper.toPromptTemplateVersionResponseDTO(version));
    }
    
    @Operation(summary = "版本回滚", description = "回滚到指定版本")
    @PostMapping("/rollback/{versionNumber}")
    public Result<PromptTemplateVersionResponseDTO> rollbackToVersion(
            @PathVariable Long templateId,
            @PathVariable Integer versionNumber) {
        PromptTemplateVersion version = versionService.rollbackToVersion(templateId, versionNumber);
        return Result.success(entityDTOMapper.toPromptTemplateVersionResponseDTO(version));
    }
    
    @Operation(summary = "版本对比", description = "对比两个版本的差异")
    @GetMapping("/compare")
    public Result<Map<String, Object>> compareVersions(
            @PathVariable Long templateId,
            @RequestParam Integer versionNumber1,
            @RequestParam Integer versionNumber2) {
        Map<String, Object> result = versionService.compareVersions(templateId, versionNumber1, versionNumber2);
        return Result.success(result);
    }
    
    @Operation(summary = "删除版本", description = "删除指定版本（不能删除当前版本）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteVersion(@PathVariable Long id) {
        versionService.deleteVersion(id);
        return Result.success();
    }
}
