package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.UserFeedback;
import com.sinosoft.testdesign.repository.UserFeedbackRepository;
import com.sinosoft.testdesign.service.UserFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户反馈服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackServiceImpl implements UserFeedbackService {
    
    private final UserFeedbackRepository feedbackRepository;
    
    @Override
    @Transactional
    public UserFeedback createFeedback(UserFeedback feedback) {
        if (feedback.getFeedbackTime() == null) {
            feedback.setFeedbackTime(LocalDateTime.now());
        }
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(LocalDateTime.now());
        }
        return feedbackRepository.save(feedback);
    }
    
    @Override
    public Optional<UserFeedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }
    
    @Override
    public List<UserFeedback> findByRequestId(String requestId) {
        return feedbackRepository.findByRequestIdOrderByFeedbackTimeDesc(requestId);
    }
    
    @Override
    public List<UserFeedback> findByLogId(Long logId) {
        return feedbackRepository.findByLogIdOrderByFeedbackTimeDesc(logId);
    }
    
    @Override
    public List<UserFeedback> findByUserId(Long userId) {
        return feedbackRepository.findByUserIdOrderByFeedbackTimeDesc(userId);
    }
    
    @Override
    public List<UserFeedback> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return feedbackRepository.findByFeedbackTimeBetweenOrderByFeedbackTimeDesc(startTime, endTime);
    }
    
    @Override
    public Page<UserFeedback> findAll(Pageable pageable) {
        return feedbackRepository.findAll(pageable);
    }
    
    @Override
    @Transactional
    public UserFeedback resolveFeedback(Long feedbackId, Long resolvedBy, String resolvedNote) {
        Optional<UserFeedback> feedback = feedbackRepository.findById(feedbackId);
        if (feedback.isEmpty()) {
            throw new IllegalArgumentException("反馈记录不存在: " + feedbackId);
        }
        
        UserFeedback userFeedback = feedback.get();
        userFeedback.setIsResolved(true);
        userFeedback.setResolvedBy(resolvedBy);
        userFeedback.setResolvedAt(LocalDateTime.now());
        
        return feedbackRepository.save(userFeedback);
    }
    
    @Override
    public Map<String, Object> getFeedbackStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计反馈总数
        Long totalCount = feedbackRepository.countByTimeRange(startTime, endTime);
        
        // 统计平均评分
        Double avgRating = feedbackRepository.avgRatingByTimeRange(startTime, endTime);
        
        // 统计各评分数量
        List<UserFeedback> feedbacks = feedbackRepository.findByFeedbackTimeBetweenOrderByFeedbackTimeDesc(startTime, endTime);
        Map<Integer, Long> ratingCounts = new HashMap<>();
        for (UserFeedback feedback : feedbacks) {
            if (feedback.getRating() != null) {
                ratingCounts.merge(feedback.getRating(), 1L, Long::sum);
            }
        }
        
        // 统计已解决和未解决数量
        long resolvedCount = feedbacks.stream().filter(f -> Boolean.TRUE.equals(f.getIsResolved())).count();
        long unresolvedCount = feedbacks.size() - resolvedCount;
        
        stats.put("totalCount", totalCount);
        stats.put("avgRating", avgRating != null ? avgRating : 0.0);
        stats.put("ratingCounts", ratingCounts);
        stats.put("resolvedCount", resolvedCount);
        stats.put("unresolvedCount", unresolvedCount);
        
        return stats;
    }
}
