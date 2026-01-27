package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.UserFeedback;
import com.sinosoft.testdesign.service.UserFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户反馈控制器
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Tag(name = "用户反馈", description = "用户反馈管理相关接口")
@RestController
@RequestMapping("/v1/user-feedback")
@RequiredArgsConstructor
public class UserFeedbackController {
    
    private final UserFeedbackService feedbackService;
    
    @Operation(summary = "创建用户反馈", description = "创建新的用户反馈")
    @PostMapping
    public Result<UserFeedback> createFeedback(@Valid @RequestBody UserFeedback feedback) {
        UserFeedback saved = feedbackService.createFeedback(feedback);
        return Result.success(saved);
    }
    
    @Operation(summary = "查询反馈", description = "根据ID查询反馈")
    @GetMapping("/{id}")
    public Result<UserFeedback> getFeedback(@PathVariable Long id) {
        Optional<UserFeedback> feedback = feedbackService.findById(id);
        return feedback.map(Result::success)
                .orElse(Result.error("反馈不存在"));
    }
    
    @Operation(summary = "根据请求ID查询反馈", description = "根据请求ID查询反馈列表")
    @GetMapping("/request/{requestId}")
    public Result<List<UserFeedback>> getFeedbackByRequestId(@PathVariable String requestId) {
        List<UserFeedback> feedbacks = feedbackService.findByRequestId(requestId);
        return Result.success(feedbacks);
    }
    
    @Operation(summary = "根据日志ID查询反馈", description = "根据日志ID查询反馈列表")
    @GetMapping("/log/{logId}")
    public Result<List<UserFeedback>> getFeedbackByLogId(@PathVariable Long logId) {
        List<UserFeedback> feedbacks = feedbackService.findByLogId(logId);
        return Result.success(feedbacks);
    }
    
    @Operation(summary = "根据用户ID查询反馈", description = "根据用户ID查询反馈列表")
    @GetMapping("/user/{userId}")
    public Result<List<UserFeedback>> getFeedbackByUserId(@PathVariable Long userId) {
        List<UserFeedback> feedbacks = feedbackService.findByUserId(userId);
        return Result.success(feedbacks);
    }
    
    @Operation(summary = "根据时间范围查询反馈", description = "根据时间范围查询反馈列表")
    @GetMapping("/time-range")
    public Result<List<UserFeedback>> getFeedbackByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<UserFeedback> feedbacks = feedbackService.findByTimeRange(startTime, endTime);
        return Result.success(feedbacks);
    }
    
    @Operation(summary = "分页查询反馈", description = "分页查询反馈列表")
    @GetMapping
    public Result<Page<UserFeedback>> getFeedbackList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserFeedback> feedbacks = feedbackService.findAll(pageable);
        return Result.success(feedbacks);
    }
    
    @Operation(summary = "解决反馈", description = "标记反馈为已解决")
    @PutMapping("/{id}/resolve")
    public Result<UserFeedback> resolveFeedback(
            @PathVariable Long id,
            @RequestParam Long resolvedBy,
            @RequestParam(required = false) String resolvedNote) {
        UserFeedback feedback = feedbackService.resolveFeedback(id, resolvedBy, resolvedNote);
        return Result.success(feedback);
    }
    
    @Operation(summary = "获取反馈统计", description = "获取反馈统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getFeedbackStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, Object> stats = feedbackService.getFeedbackStats(startTime, endTime);
        return Result.success(stats);
    }
}
