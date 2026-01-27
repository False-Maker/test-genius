package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.AppLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用日志服务接口
 * 用于记录和管理LLM应用调用日志
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface AppLogService {
    
    /**
     * 记录应用日志
     */
    void log(AppLog appLog);
    
    /**
     * 异步记录应用日志
     */
    void logAsync(AppLog appLog);
    
    /**
     * 根据ID查询日志
     */
    AppLog findById(Long id);
    
    /**
     * 根据请求ID查询日志
     */
    AppLog findByRequestId(String requestId);
    
    /**
     * 根据用户ID查询日志
     */
    List<AppLog> findByUserId(Long userId);
    
    /**
     * 根据应用类型查询日志
     */
    List<AppLog> findByAppType(String appType);
    
    /**
     * 根据模型代码查询日志
     */
    List<AppLog> findByModelCode(String modelCode);
    
    /**
     * 根据时间范围查询日志
     */
    List<AppLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询日志
     */
    Page<AppLog> findAll(Pageable pageable);
    
    /**
     * 统计指定时间范围内的性能指标
     */
    Map<String, Object> getPerformanceStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的成本
     */
    Map<String, Object> getCostStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定时间范围内的模型使用情况
     */
    Map<String, Object> getModelUsageStats(LocalDateTime startTime, LocalDateTime endTime);
}
