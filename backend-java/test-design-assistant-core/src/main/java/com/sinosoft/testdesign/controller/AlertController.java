package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.AlertRecord;
import com.sinosoft.testdesign.entity.AlertRule;
import com.sinosoft.testdesign.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 告警管理控制器
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Tag(name = "告警管理", description = "告警规则和告警记录管理相关接口")
@RestController
@RequestMapping("/v1/alerts")
@RequiredArgsConstructor
public class AlertController {
    
    private final AlertService alertService;
    
    // ========== 告警规则管理 ==========
    
    @Operation(summary = "创建告警规则", description = "创建新的告警规则")
    @PostMapping("/rules")
    public Result<AlertRule> createAlertRule(@Valid @RequestBody AlertRule rule) {
        AlertRule saved = alertService.createAlertRule(rule);
        return Result.success(saved);
    }
    
    @Operation(summary = "更新告警规则", description = "更新告警规则")
    @PutMapping("/rules/{id}")
    public Result<AlertRule> updateAlertRule(
            @PathVariable Long id,
            @Valid @RequestBody AlertRule rule) {
        rule.setId(id);
        AlertRule updated = alertService.updateAlertRule(rule);
        return Result.success(updated);
    }
    
    @Operation(summary = "查询告警规则", description = "根据ID查询告警规则")
    @GetMapping("/rules/{id}")
    public Result<AlertRule> getAlertRule(@PathVariable Long id) {
        Optional<AlertRule> rule = alertService.findRuleById(id);
        return rule.map(Result::success)
                .orElse(Result.error("告警规则不存在"));
    }
    
    @Operation(summary = "根据规则代码查询告警规则", description = "根据规则代码查询告警规则")
    @GetMapping("/rules/code/{ruleCode}")
    public Result<AlertRule> getAlertRuleByCode(@PathVariable String ruleCode) {
        Optional<AlertRule> rule = alertService.findRuleByCode(ruleCode);
        return rule.map(Result::success)
                .orElse(Result.error("告警规则不存在"));
    }
    
    @Operation(summary = "查询所有启用的告警规则", description = "查询所有启用的告警规则列表")
    @GetMapping("/rules/enabled")
    public Result<List<AlertRule>> getEnabledAlertRules() {
        List<AlertRule> rules = alertService.findAllEnabledRules();
        return Result.success(rules);
    }
    
    @Operation(summary = "查询所有告警规则", description = "查询所有告警规则列表")
    @GetMapping("/rules")
    public Result<List<AlertRule>> getAllAlertRules() {
        List<AlertRule> rules = alertService.findAllRules();
        return Result.success(rules);
    }
    
    @Operation(summary = "删除告警规则", description = "根据ID删除告警规则")
    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteAlertRule(@PathVariable Long id) {
        alertService.deleteRuleById(id);
        return Result.success();
    }
    
    @Operation(summary = "启用/禁用告警规则", description = "启用或禁用告警规则")
    @PutMapping("/rules/{id}/toggle-enabled")
    public Result<AlertRule> toggleRuleEnabled(
            @PathVariable Long id,
            @RequestParam Boolean isEnabled) {
        AlertRule rule = alertService.toggleRuleEnabled(id, isEnabled);
        return Result.success(rule);
    }
    
    @Operation(summary = "手动触发告警检查", description = "手动触发告警规则检查")
    @PostMapping("/rules/check")
    public Result<Void> checkAlertRules() {
        alertService.checkAlertRules();
        return Result.success();
    }
    
    // ========== 告警记录管理 ==========
    
    @Operation(summary = "查询告警记录", description = "分页查询告警记录")
    @GetMapping("/records")
    public Result<Page<AlertRecord>> getAlertRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AlertRecord> records = alertService.findAlertRecords(pageable);
        return Result.success(records);
    }
    
    @Operation(summary = "查询未解决的告警记录", description = "查询所有未解决的告警记录")
    @GetMapping("/records/unresolved")
    public Result<List<AlertRecord>> getUnresolvedAlerts() {
        List<AlertRecord> records = alertService.findUnresolvedAlerts();
        return Result.success(records);
    }
    
    @Operation(summary = "解决告警", description = "标记告警为已解决")
    @PutMapping("/records/{id}/resolve")
    public Result<AlertRecord> resolveAlert(
            @PathVariable Long id,
            @RequestParam Long resolvedBy,
            @RequestParam(required = false) String resolvedNote) {
        AlertRecord record = alertService.resolveAlert(id, resolvedBy, resolvedNote);
        return Result.success(record);
    }
}
