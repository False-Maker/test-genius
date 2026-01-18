package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.UIScriptTemplateRequestDTO;
import com.sinosoft.testdesign.dto.UIScriptTemplateResponseDTO;
import com.sinosoft.testdesign.entity.UIScriptTemplate;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.UIScriptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UI脚本模板控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "UI脚本模板", description = "UI脚本模板管理相关接口")
@RestController
@RequestMapping("/v1/ui-script-templates")
@RequiredArgsConstructor
public class UIScriptTemplateController {
    
    private final UIScriptTemplateService templateService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建脚本模板", description = "创建新的UI脚本模板")
    @PostMapping
    public Result<UIScriptTemplateResponseDTO> createTemplate(@Valid @RequestBody UIScriptTemplateRequestDTO dto) {
        UIScriptTemplate template = entityDTOMapper.toUIScriptTemplateEntity(dto);
        UIScriptTemplate saved = templateService.createTemplate(template);
        return Result.success(entityDTOMapper.toUIScriptTemplateResponseDTO(saved));
    }
    
    @Operation(summary = "查询脚本模板列表", description = "分页查询脚本模板列表，支持按模板名称、类型、语言、启用状态搜索")
    @GetMapping
    public Result<Page<UIScriptTemplateResponseDTO>> getTemplateList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String scriptLanguage,
            @RequestParam(required = false) String isActive) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UIScriptTemplate> templatePage = templateService.getTemplateList(pageable, templateName, templateType, scriptLanguage, isActive);
        
        Page<UIScriptTemplateResponseDTO> dtoPage = templatePage.map(entityDTOMapper::toUIScriptTemplateResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取脚本模板详情", description = "根据ID获取脚本模板详情")
    @GetMapping("/{id}")
    public Result<UIScriptTemplateResponseDTO> getTemplateById(@PathVariable Long id) {
        UIScriptTemplate template = templateService.getTemplateById(id);
        return Result.success(entityDTOMapper.toUIScriptTemplateResponseDTO(template));
    }
    
    @Operation(summary = "根据模板编码获取脚本模板详情", description = "根据模板编码获取脚本模板详情")
    @GetMapping("/code/{templateCode}")
    public Result<UIScriptTemplateResponseDTO> getTemplateByCode(@PathVariable String templateCode) {
        UIScriptTemplate template = templateService.getTemplateByCode(templateCode);
        return Result.success(entityDTOMapper.toUIScriptTemplateResponseDTO(template));
    }
    
    @Operation(summary = "更新脚本模板", description = "更新脚本模板信息")
    @PutMapping("/{id}")
    public Result<UIScriptTemplateResponseDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UIScriptTemplateRequestDTO dto) {
        UIScriptTemplate template = entityDTOMapper.toUIScriptTemplateEntity(dto);
        UIScriptTemplate updated = templateService.updateTemplate(id, template);
        return Result.success(entityDTOMapper.toUIScriptTemplateResponseDTO(updated));
    }
    
    @Operation(summary = "删除脚本模板", description = "删除指定脚本模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }
    
    @Operation(summary = "查询启用的模板列表", description = "根据模板类型和脚本语言查询启用的模板列表")
    @GetMapping("/active")
    public Result<List<UIScriptTemplateResponseDTO>> getActiveTemplatesByTypeAndLanguage(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String scriptLanguage) {
        List<UIScriptTemplate> templates = templateService.getActiveTemplatesByTypeAndLanguage(templateType, scriptLanguage);
        List<UIScriptTemplateResponseDTO> dtoList = templates.stream()
                .map(entityDTOMapper::toUIScriptTemplateResponseDTO)
                .toList();
        return Result.success(dtoList);
    }
    
    @Operation(summary = "启用/禁用模板", description = "更新模板启用状态")
    @PutMapping("/{id}/status")
    public Result<UIScriptTemplateResponseDTO> updateTemplateStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        UIScriptTemplate updated = templateService.updateTemplateStatus(id, isActive);
        return Result.success(entityDTOMapper.toUIScriptTemplateResponseDTO(updated));
    }
}

