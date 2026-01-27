package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.repository.AppLogRepository;
import com.sinosoft.testdesign.service.AppLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用日志服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppLogServiceImpl implements AppLogService {
    
    private final AppLogRepository appLogRepository;
    
    @Override
    @Transactional
    public void log(AppLog appLog) {
        try {
            if (appLog.getTimestamp() == null) {
                appLog.setTimestamp(LocalDateTime.now());
            }
            if (appLog.getCreatedAt() == null) {
                appLog.setCreatedAt(LocalDateTime.now());
            }
            appLogRepository.save(appLog);
        } catch (Exception e) {
            log.error("记录应用日志失败", e);
            // 日志记录失败不应该影响主业务流程，只记录错误日志
        }
    }
    
    @Override
    @Async
    @Transactional
    public void logAsync(AppLog appLog) {
        log(appLog);
    }
    
    @Override
    public AppLog findById(Long id) {
        return appLogRepository.findById(id).orElse(null);
    }
    
    @Override
    public AppLog findByRequestId(String requestId) {
        return appLogRepository.findByRequestId(requestId);
    }
    
    @Override
    public List<AppLog> findByUserId(Long userId) {
        return appLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    @Override
    public List<AppLog> findByAppType(String appType) {
        return appLogRepository.findByAppTypeOrderByTimestampDesc(appType);
    }
    
    @Override
    public List<AppLog> findByModelCode(String modelCode) {
        return appLogRepository.findByModelCodeOrderByTimestampDesc(modelCode);
    }
    
    @Override
    public List<AppLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return appLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
    }
    
    @Override
    public Page<AppLog> findAll(Pageable pageable) {
        return appLogRepository.findAll(pageable);
    }
    
    @Override
    public Map<String, Object> getPerformanceStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        Long successCount = appLogRepository.countSuccessByTimeRange(startTime, endTime);
        Long failedCount = appLogRepository.countFailedByTimeRange(startTime, endTime);
        Long totalCount = successCount + failedCount;
        
        Double avgResponseTime = appLogRepository.avgResponseTimeByTimeRange(startTime, endTime);
        
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("successRate", totalCount > 0 ? (double) successCount / totalCount * 100 : 0);
        stats.put("failureRate", totalCount > 0 ? (double) failedCount / totalCount * 100 : 0);
        stats.put("avgResponseTime", avgResponseTime != null ? avgResponseTime : 0);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getCostStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        BigDecimal totalCost = appLogRepository.sumCostByTimeRange(startTime, endTime);
        Long totalTokens = appLogRepository.sumTokensByTimeRange(startTime, endTime);
        
        stats.put("totalCost", totalCost != null ? totalCost : BigDecimal.ZERO);
        stats.put("totalTokens", totalTokens != null ? totalTokens : 0);
        stats.put("avgCostPerRequest", totalCost != null && totalCost.compareTo(BigDecimal.ZERO) > 0 
            ? totalCost.divide(new BigDecimal(appLogRepository.countSuccessByTimeRange(startTime, endTime)), 6, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getModelUsageStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        // 这里可以扩展为按模型统计使用情况
        // 目前先返回基础统计
        Long totalCount = appLogRepository.countSuccessByTimeRange(startTime, endTime) 
            + appLogRepository.countFailedByTimeRange(startTime, endTime);
        
        stats.put("totalRequests", totalCount);
        
        return stats;
    }
}
