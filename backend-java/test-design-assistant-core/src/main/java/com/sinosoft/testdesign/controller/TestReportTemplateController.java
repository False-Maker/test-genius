package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestReportTemplateRequestDTO;
import com.sinosoft.testdesign.dto.TestReportTemplateResponseDTO;
import com.sinosoft.testdesign.entity.TestReportTemplate;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestReportTemplateService;
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
 * 测试报告模板管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "测试报告模板管理", description = "测试报告模板管理相关接口")
@RestController
@RequestMapping("/v1/test-report-templates")
@RequiredArgsConstructor
public class TestReportTemplateController {
    
    private final TestReportTemplateService templateService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建报告模板", description = "创建新的测试报告模板")
    @PostMapping
    public Result<TestReportTemplateResponseDTO> createTemplate(@Valid @RequestBody TestReportTemplateRequestDTO dto) {
        TestReportTemplate template = entityDTOMapper.toTestReportTemplateEntity(dto);
        TestReportTemplate saved = templateService.createTemplate(template);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(saved));
    }
    
    @Operation(summary = "查询报告模板列表", description = "分页查询测试报告模板列表")
    @GetMapping
    public Result<Page<TestReportTemplateResponseDTO>> getTemplateList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestReportTemplate> templatePage = templateService.getTemplateList(pageable);
        
        // 转换为DTO分页
        Page<TestReportTemplateResponseDTO> dtoPage = templatePage.map(entityDTOMapper::toTestReportTemplateResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取报告模板详情", description = "根据ID获取报告模板详情")
    @GetMapping("/{id}")
    public Result<TestReportTemplateResponseDTO> getTemplateById(@PathVariable Long id) {
        TestReportTemplate template = templateService.getTemplateById(id);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(template));
    }
    
    @Operation(summary = "根据编码获取报告模板详情", description = "根据模板编码获取报告模板详情")
    @GetMapping("/code/{templateCode}")
    public Result<TestReportTemplateResponseDTO> getTemplateByCode(@PathVariable String templateCode) {
        TestReportTemplate template = templateService.getTemplateByCode(templateCode);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(template));
    }
    
    @Operation(summary = "更新报告模板", description = "更新测试报告模板")
    @PutMapping("/{id}")
    public Result<TestReportTemplateResponseDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TestReportTemplateRequestDTO dto) {
        TestReportTemplate template = templateService.getTemplateById(id);
        entityDTOMapper.updateTestReportTemplateFromDTO(dto, template);
        TestReportTemplate updated = templateService.updateTemplate(id, template);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(updated));
    }
    
    @Operation(summary = "删除报告模板", description = "删除指定报告模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }
    
    @Operation(summary = "启用/禁用报告模板", description = "切换报告模板的启用状态")
    @PutMapping("/{id}/status")
    public Result<TestReportTemplateResponseDTO> toggleTemplateStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        TestReportTemplate updated = templateService.toggleTemplateStatus(id, isActive);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(updated));
    }
    
    @Operation(summary = "设置默认模板", description = "设置指定类型的默认报告模板")
    @PutMapping("/{id}/default")
    public Result<TestReportTemplateResponseDTO> setDefaultTemplate(
            @PathVariable Long id,
            @RequestParam String templateType) {
        TestReportTemplate updated = templateService.setDefaultTemplate(id, templateType);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(updated));
    }
    
    @Operation(summary = "查询启用的模板列表", description = "根据类型查询启用的报告模板列表")
    @GetMapping("/active")
    public Result<List<TestReportTemplateResponseDTO>> getActiveTemplatesByType(
            @RequestParam String templateType) {
        List<TestReportTemplate> templates = templateService.getActiveTemplatesByType(templateType);
        List<TestReportTemplateResponseDTO> dtoList = templates.stream()
                .map(entityDTOMapper::toTestReportTemplateResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "查询默认模板", description = "根据类型查询默认报告模板")
    @GetMapping("/default")
    public Result<TestReportTemplateResponseDTO> getDefaultTemplateByType(
            @RequestParam String templateType) {
        TestReportTemplate template = templateService.getDefaultTemplateByType(templateType);
        return Result.success(entityDTOMapper.toTestReportTemplateResponseDTO(template));
    }
}

