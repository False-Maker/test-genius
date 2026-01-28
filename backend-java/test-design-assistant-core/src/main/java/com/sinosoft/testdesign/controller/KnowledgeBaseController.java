package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.service.KnowledgeBaseManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器（第四阶段增强）
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Slf4j
@Tag(name = "知识库管理", description = "知识库管理相关接口")
@RestController
@RequestMapping("/api/v1/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    
    private final KnowledgeBaseManageService knowledgeBaseManageService;
    
    @Operation(summary = "创建知识库", description = "创建新的知识库")
    @PostMapping
    public Result<Long> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseRequestDTO request) {
        log.info("创建知识库请求: {}", request.getKbName());
        Long id = knowledgeBaseManageService.createKnowledgeBase(request);
        return Result.success(id);
    }
    
    @Operation(summary = "更新知识库", description = "更新知识库信息")
    @PutMapping("/{id}")
    public Result<Void> updateKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @Valid @RequestBody KnowledgeBaseRequestDTO request) {
        log.info("更新知识库请求: ID={}", id);
        knowledgeBaseManageService.updateKnowledgeBase(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除知识库", description = "删除指定知识库")
    @DeleteMapping("/{id}")
    public Result<Void> deleteKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        log.info("删除知识库请求: ID={}", id);
        knowledgeBaseManageService.deleteKnowledgeBase(id);
        return Result.success();
    }
    
    @Operation(summary = "获取知识库详情", description = "获取指定知识库的详细信息")
    @GetMapping("/{id}")
    public Result<KnowledgeBaseResponseDTO> getKnowledgeBaseById(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        KnowledgeBaseResponseDTO dto = knowledgeBaseManageService.getKnowledgeBaseById(id);
        return Result.success(dto);
    }
    
    @Operation(summary = "获取知识库列表", description = "获取所有激活的知识库列表")
    @GetMapping
    public Result<List<KnowledgeBaseResponseDTO>> getKnowledgeBaseList() {
        List<KnowledgeBaseResponseDTO> list = knowledgeBaseManageService.getKnowledgeBaseList();
        return Result.success(list);
    }
    
    @Operation(summary = "获取用户的知识库列表", description = "获取用户有权限的知识库列表")
    @GetMapping("/user/{userId}")
    public Result<List<KnowledgeBaseResponseDTO>> getUserKnowledgeBaseList(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "权限类型（可选）") @RequestParam(required = false) String permissionType) {
        List<KnowledgeBaseResponseDTO> list = knowledgeBaseManageService.getUserKnowledgeBaseList(userId, permissionType);
        return Result.success(list);
    }
    
    @Operation(summary = "授予权限", description = "为用户授予知识库访问权限")
    @PostMapping("/permission/grant")
    public Result<Boolean> grantPermission(@Valid @RequestBody KnowledgePermissionRequestDTO request) {
        log.info("授予权限请求: kbId={}, userId={}", request.getKbId(), request.getUserId());
        boolean result = knowledgeBaseManageService.grantPermission(request);
        return Result.success(result);
    }
    
    @Operation(summary = "撤销权限", description = "撤销用户的知识库访问权限")
    @DeleteMapping("/permission/revoke")
    public Result<Boolean> revokePermission(
            @Parameter(description = "知识库ID") @RequestParam Long kbId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "权限类型") @RequestParam String permissionType) {
        log.info("撤销权限请求: kbId={}, userId={}", kbId, userId);
        boolean result = knowledgeBaseManageService.revokePermission(kbId, userId, permissionType);
        return Result.success(result);
    }
    
    @Operation(summary = "检查权限", description = "检查用户是否有指定权限")
    @GetMapping("/permission/check")
    public Result<Boolean> checkPermission(
            @Parameter(description = "知识库ID") @RequestParam Long kbId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "权限类型") @RequestParam String permissionType) {
        boolean hasPermission = knowledgeBaseManageService.checkPermission(kbId, userId, permissionType);
        return Result.success(hasPermission);
    }
    
    @Operation(summary = "获取知识库权限列表", description = "获取指定知识库的所有权限")
    @GetMapping("/{kbId}/permissions")
    public Result<List<KnowledgePermissionResponseDTO>> getKnowledgeBasePermissions(
            @Parameter(description = "知识库ID") @PathVariable Long kbId) {
        List<KnowledgePermissionResponseDTO> list = knowledgeBaseManageService.getKnowledgeBasePermissions(kbId);
        return Result.success(list);
    }
    
    @Operation(summary = "获取用户权限列表", description = "获取用户的所有知识库权限")
    @GetMapping("/user/{userId}/permissions")
    public Result<List<KnowledgePermissionResponseDTO>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<KnowledgePermissionResponseDTO> list = knowledgeBaseManageService.getUserPermissions(userId);
        return Result.success(list);
    }
    
    @Operation(summary = "同步知识库", description = "同步知识库文档（增量/全量）")
    @PostMapping("/{kbId}/sync")
    public Result<Map<String, Object>> syncKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @Parameter(description = "同步类型（incremental/full）") @RequestParam String syncType,
            @Parameter(description = "源文件路径") @RequestParam String sourcePath) {
        log.info("同步知识库请求: kbId={}, syncType={}", kbId, syncType);
        Map<String, Object> result = knowledgeBaseManageService.syncKnowledgeBase(kbId, syncType, sourcePath);
        return Result.success(result);
    }
    
    @Operation(summary = "获取同步日志", description = "获取知识库的同步日志列表")
    @GetMapping("/{kbId}/sync-logs")
    public Result<List<KnowledgeBaseSyncLogResponseDTO>> getSyncLogs(
            @Parameter(description = "知识库ID") @PathVariable Long kbId) {
        List<KnowledgeBaseSyncLogResponseDTO> logs = knowledgeBaseManageService.getSyncLogs(kbId);
        return Result.success(logs);
    }
    
    @Operation(summary = "上传文档", description = "上传文档到知识库")
    @PostMapping("/{kbId}/upload")
    public Result<String> uploadDocument(
            @Parameter(description = "知识库ID") @PathVariable Long kbId,
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "创建人ID") @RequestParam Long creatorId) {
        log.info("上传文档请求: kbId={}, fileName={}", kbId, file.getOriginalFilename());
        try {
            String docCode = knowledgeBaseManageService.uploadDocument(
                    kbId, 
                    file.getOriginalFilename(), 
                    file.getBytes(), 
                    creatorId
            );
            return Result.success(docCode);
        } catch (Exception e) {
            log.error("上传文档失败", e);
            return Result.error(600, "上传文档失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "生成知识库编码", description = "生成新的知识库编码")
    @GetMapping("/generate-code")
    public Result<String> generateKbCode() {
        String code = knowledgeBaseManageService.generateKbCode();
        return Result.success(code);
    }
}
