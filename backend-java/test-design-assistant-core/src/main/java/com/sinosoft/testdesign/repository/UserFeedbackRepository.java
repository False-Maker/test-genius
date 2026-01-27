package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户反馈Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long>, JpaSpecificationExecutor<UserFeedback> {
    
    /**
     * 根据请求ID查询反馈
     */
    List<UserFeedback> findByRequestIdOrderByFeedbackTimeDesc(String requestId);
    
    /**
     * 根据日志ID查询反馈
     */
    List<UserFeedback> findByLogIdOrderByFeedbackTimeDesc(Long logId);
    
    /**
     * 根据用户ID查询反馈
     */
    List<UserFeedback> findByUserIdOrderByFeedbackTimeDesc(Long userId);
    
    /**
     * 根据评分查询反馈
     */
    List<UserFeedback> findByRatingOrderByFeedbackTimeDesc(Integer rating);
    
    /**
     * 根据是否已处理查询反馈
     */
    List<UserFeedback> findByIsResolvedOrderByFeedbackTimeDesc(Boolean isResolved);
    
    /**
     * 根据时间范围查询反馈
     */
    List<UserFeedback> findByFeedbackTimeBetweenOrderByFeedbackTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的平均评分
     */
    @Query("SELECT AVG(u.rating) FROM UserFeedback u WHERE u.feedbackTime BETWEEN :startTime AND :endTime")
    Double avgRatingByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的反馈总数
     */
    @Query("SELECT COUNT(u) FROM UserFeedback u WHERE u.feedbackTime BETWEEN :startTime AND :endTime")
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
