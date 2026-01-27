package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AppLog;
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
 * 应用日志Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface AppLogRepository extends JpaRepository<AppLog, Long>, JpaSpecificationExecutor<AppLog> {
    
    /**
     * 根据请求ID查询日志
     */
    AppLog findByRequestId(String requestId);
    
    /**
     * 根据用户ID查询日志
     */
    List<AppLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    /**
     * 根据应用类型查询日志
     */
    List<AppLog> findByAppTypeOrderByTimestampDesc(String appType);
    
    /**
     * 根据模型代码查询日志
     */
    List<AppLog> findByModelCodeOrderByTimestampDesc(String modelCode);
    
    /**
     * 根据状态查询日志
     */
    List<AppLog> findByStatusOrderByTimestampDesc(String status);
    
    /**
     * 根据时间范围查询日志
     */
    List<AppLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据用户ID和时间范围查询日志
     */
    List<AppLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据应用类型和时间范围查询日志
     */
    List<AppLog> findByAppTypeAndTimestampBetweenOrderByTimestampDesc(String appType, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据模型代码和时间范围查询日志
     */
    List<AppLog> findByModelCodeAndTimestampBetweenOrderByTimestampDesc(String modelCode, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的成功请求数
     */
    @Query("SELECT COUNT(a) FROM AppLog a WHERE a.status = 'success' AND a.timestamp BETWEEN :startTime AND :endTime")
    Long countSuccessByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的失败请求数
     */
    @Query("SELECT COUNT(a) FROM AppLog a WHERE a.status = 'failed' AND a.timestamp BETWEEN :startTime AND :endTime")
    Long countFailedByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的总token数
     */
    @Query("SELECT COALESCE(SUM(a.tokensTotal), 0) FROM AppLog a WHERE a.timestamp BETWEEN :startTime AND :endTime")
    Long sumTokensByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的总成本
     */
    @Query("SELECT COALESCE(SUM(a.cost), 0) FROM AppLog a WHERE a.timestamp BETWEEN :startTime AND :endTime")
    java.math.BigDecimal sumCostByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的平均响应时间
     */
    @Query("SELECT AVG(a.responseTime) FROM AppLog a WHERE a.status = 'success' AND a.timestamp BETWEEN :startTime AND :endTime")
    Double avgResponseTimeByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
