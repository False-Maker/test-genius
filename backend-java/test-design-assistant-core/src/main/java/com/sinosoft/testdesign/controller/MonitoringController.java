package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 监控统计控制器
 * 提供性能监控和统计分析API
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Tag(name = "监控统计", description = "性能监控和统计分析相关接口")
@RestController
@RequestMapping("/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    
    private final MonitoringService monitoringService;
    
    @Operation(summary = "获取性能统计", description = "获取指定时间范围内的性能统计信息")
    @GetMapping("/performance")
    public Result<Map<String, Object>> getPerformanceStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String appType,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = monitoringService.getPerformanceStats(
                startTime, endTime, modelCode, appType, userId);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取响应时间统计", description = "获取响应时间统计（P50、P95、P99）")
    @GetMapping("/response-time")
    public Result<Map<String, Object>> getResponseTimeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String appType) {
        Map<String, Object> stats = monitoringService.getResponseTimeStats(
                startTime, endTime, modelCode, appType);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取成功率统计", description = "获取成功率统计信息")
    @GetMapping("/success-rate")
    public Result<Map<String, Object>> getSuccessRateStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String appType) {
        Map<String, Object> stats = monitoringService.getSuccessRateStats(
                startTime, endTime, modelCode, appType);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取Token使用量统计", description = "获取Token使用量统计信息")
    @GetMapping("/token-usage")
    public Result<Map<String, Object>> getTokenUsageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String appType) {
        Map<String, Object> stats = monitoringService.getTokenUsageStats(
                startTime, endTime, modelCode, appType);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取成本统计", description = "获取成本统计信息")
    @GetMapping("/cost")
    public Result<Map<String, Object>> getCostStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String appType,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = monitoringService.getCostStats(
                startTime, endTime, modelCode, appType, userId);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取模型使用情况统计", description = "获取各模型的使用情况统计")
    @GetMapping("/model-usage")
    public Result<Map<String, Object>> getModelUsageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, Object> stats = monitoringService.getModelUsageStats(startTime, endTime);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取应用使用情况统计", description = "获取各应用的使用情况统计")
    @GetMapping("/app-usage")
    public Result<Map<String, Object>> getAppUsageStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, Object> stats = monitoringService.getAppUsageStats(startTime, endTime);
        return Result.success(stats);
    }
    
    @Operation(summary = "获取时间序列数据", description = "获取时间序列数据（用于图表展示）")
    @GetMapping("/time-series")
    public Result<Map<String, Object>> getTimeSeriesData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam String interval, // HOUR/DAY/WEEK/MONTH
            @RequestParam String metric, // RESPONSE_TIME/SUCCESS_RATE/TOKEN_USAGE/COST
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String appType) {
        Map<String, Object> data = monitoringService.getTimeSeriesData(
                startTime, endTime, interval, metric, modelCode, appType);
        return Result.success(data);
    }

    @Operation(summary = "获取模型性能对比统计", description = "获取各模型的性能对比数据（包括成功率、响应时间、成本、评分等）")
    @GetMapping("/model-performance")
    public Result<java.util.List<Map<String, Object>>> getModelPerformanceStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        java.util.List<Map<String, Object>> stats = monitoringService.getModelPerformanceStats(startTime, endTime);
        return Result.success(stats);
    }
}
