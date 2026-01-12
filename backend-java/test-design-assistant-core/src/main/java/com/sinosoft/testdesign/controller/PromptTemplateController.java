package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.PromptTemplate;
import com.sinosoft.testdesign.service.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 提示词模板管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "提示词模板管理", description = "提示词模板管理相关接口")
@RestController
@RequestMapping("/v1/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {
    
    private final PromptTemplateService templateService;
    
    @Operation(summary = "创建模板", description = "创建新的提示词模板")
    @PostMapping
    public Result<PromptTemplate> createTemplate(@RequestBody PromptTemplate template) {
        return Result.success(templateService.createTemplate(template));
    }
    
    @Operation(summary = "查询模板列表", description = "分页查询提示词模板列表")
    @GetMapping
    public Result<Page<PromptTemplate>> getTemplateList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(templateService.getTemplateList(pageable));
    }
    
    @Operation(summary = "获取模板详情", description = "根据ID获取模板详情")
    @GetMapping("/{id}")
    public Result<PromptTemplate> getTemplateById(@PathVariable Long id) {
        return Result.success(templateService.getTemplateById(id));
    }
    
    @Operation(summary = "更新模板", description = "更新提示词模板")
    @PutMapping("/{id}")
    public Result<PromptTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody PromptTemplate template) {
        return Result.success(templateService.updateTemplate(id, template));
    }
    
    @Operation(summary = "删除模板", description = "删除指定模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }
    
    @Operation(summary = "启用/禁用模板", description = "切换模板的启用状态")
    @PutMapping("/{id}/status")
    public Result<PromptTemplate> toggleTemplateStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        return Result.success(templateService.toggleTemplateStatus(id, isActive));
    }
    
    @Operation(summary = "生成提示词", description = "根据模板ID和变量生成提示词")
    @PostMapping("/{id}/generate")
    public Result<String> generatePrompt(
            @PathVariable Long id,
            @RequestBody Map<String, Object> variables) {
        return Result.success(templateService.generatePrompt(id, variables));
    }
    
    @Operation(summary = "生成提示词（自定义模板）", description = "根据自定义模板内容和变量生成提示词")
    @PostMapping("/generate")
    public Result<String> generatePromptWithContent(
            @RequestParam String templateContent,
            @RequestBody Map<String, Object> variables) {
        return Result.success(templateService.generatePrompt(templateContent, variables));
    }
}

