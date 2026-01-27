package com.sinosoft.testdesign.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 监控服务接口
 * 提供性能监控和统计分析功能
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface MonitoringService {
    
    /**
     * 获取性能统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param modelCode 模型代码（可选）
     * @param appType 应用类型（可选）
     * @param userId 用户ID（可选）
     * @return 性能统计信息
     */
    Map<String, Object> getPerformanceStats(LocalDateTime startTime, LocalDateTime endTime, 
                                           String modelCode, String appType, Long userId);
    
    /**
     * 获取响应时间统计（P50、P95、P99）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param modelCode 模型代码（可选）
     * @param appType 应用类型（可选）
     * @return 响应时间统计
     */
    Map<String, Object> getResponseTimeStats(LocalDateTime startTime, LocalDateTime endTime,
                                            String modelCode, String appType);
    
    /**
     * 获取成功率统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param modelCode 模型代码（可选）
     * @param appType 应用类型（可选）
     * @return 成功率统计
     */
    Map<String, Object> getSuccessRateStats(LocalDateTime startTime, LocalDateTime endTime,
                                          String modelCode, String appType);
    
    /**
     * 获取Token使用量统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param modelCode 模型代码（可选）
     * @param appType 应用类型（可选）
     * @return Token使用量统计
     */
    Map<String, Object> getTokenUsageStats(LocalDateTime startTime, LocalDateTime endTime,
                                          String modelCode, String appType);
    
    /**
     * 获取成本统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param modelCode 模型代码（可选）
     * @param appType 应用类型（可选）
     * @param userId 用户ID（可选）
     * @return 成本统计
     */
    Map<String, Object> getCostStats(LocalDateTime startTime, LocalDateTime endTime,
                                   String modelCode, String appType, Long userId);
    
    /**
     * 获取模型使用情况统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 模型使用情况统计
     */
    Map<String, Object> getModelUsageStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取应用使用情况统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 应用使用情况统计
     */
    Map<String, Object> getAppUsageStats(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取时间序列数据（用于图表展示）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔（HOUR/DAY/WEEK/MONTH）
     * @param metric 指标类型（RESPONSE_TIME/SUCCESS_RATE/TOKEN_USAGE/COST）
     * @param modelCode 模型代码（可选）
     * @param appType 应用类型（可选）
     * @return 时间序列数据
     */
    Map<String, Object> getTimeSeriesData(LocalDateTime startTime, LocalDateTime endTime,
                                         String interval, String metric,
                                         String modelCode, String appType);
}
