package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.ApplicationRequestDTO;
import com.sinosoft.testdesign.dto.ApplicationResponseDTO;
import com.sinosoft.testdesign.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 应用管理控制器（第四阶段 4.3）
 * 应用管理 API、版本来源信息（工作流/提示词版本）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Tag(name = "应用管理", description = "应用管理、版本来源与版本控制流程")
@RestController
@RequestMapping("/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "创建应用", description = "创建新应用，可关联工作流或提示词模板")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ApplicationRequestDTO dto) {
        Long id = applicationService.create(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新应用", description = "根据 ID 更新应用")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ApplicationRequestDTO dto) {
        applicationService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除应用", description = "根据 ID 删除应用")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取应用详情", description = "根据 ID 获取应用详情")
    @GetMapping("/{id}")
    public Result<ApplicationResponseDTO> getById(@PathVariable Long id) {
        return applicationService.getById(id)
                .map(Result::success)
                .orElse(Result.error("应用不存在"));
    }

    @Operation(summary = "根据编码获取应用", description = "根据应用编码获取应用详情")
    @GetMapping("/code/{appCode}")
    public Result<ApplicationResponseDTO> getByAppCode(@PathVariable String appCode) {
        return applicationService.getByAppCode(appCode)
                .map(Result::success)
                .orElse(Result.error("应用不存在"));
    }

    @Operation(summary = "应用列表", description = "获取所有应用")
    @GetMapping
    public Result<List<ApplicationResponseDTO>> list() {
        return Result.success(applicationService.list());
    }

    @Operation(summary = "按类型列表", description = "按应用类型获取应用列表")
    @GetMapping("/type/{appType}")
    public Result<List<ApplicationResponseDTO>> listByType(@PathVariable String appType) {
        return Result.success(applicationService.listByType(appType));
    }

    @Operation(summary = "启用的应用列表", description = "获取所有启用的应用")
    @GetMapping("/active")
    public Result<List<ApplicationResponseDTO>> listActive() {
        return Result.success(applicationService.listActive());
    }

    @Operation(summary = "版本来源信息", description = "获取应用版本来源（工作流/提示词）及 versions API 路径")
    @GetMapping("/{id}/version-info")
    public Result<Map<String, Object>> getVersionInfo(@PathVariable Long id) {
        return Result.success(applicationService.getVersionInfo(id));
    }
}
