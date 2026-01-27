package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户反馈服务接口
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface UserFeedbackService {
    
    /**
     * 创建用户反馈
     */
    UserFeedback createFeedback(UserFeedback feedback);
    
    /**
     * 根据ID查询反馈
     */
    Optional<UserFeedback> findById(Long id);
    
    /**
     * 根据请求ID查询反馈
     */
    List<UserFeedback> findByRequestId(String requestId);
    
    /**
     * 根据日志ID查询反馈
     */
    List<UserFeedback> findByLogId(Long logId);
    
    /**
     * 根据用户ID查询反馈
     */
    List<UserFeedback> findByUserId(Long userId);
    
    /**
     * 根据时间范围查询反馈
     */
    List<UserFeedback> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询反馈
     */
    Page<UserFeedback> findAll(Pageable pageable);
    
    /**
     * 解决反馈
     */
    UserFeedback resolveFeedback(Long feedbackId, Long resolvedBy, String resolvedNote);
    
    /**
     * 获取反馈统计
     */
    Map<String, Object> getFeedbackStats(LocalDateTime startTime, LocalDateTime endTime);
}
