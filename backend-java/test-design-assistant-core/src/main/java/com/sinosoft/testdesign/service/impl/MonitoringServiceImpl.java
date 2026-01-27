package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.repository.AppLogRepository;
import com.sinosoft.testdesign.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监控服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    
    private final AppLogRepository appLogRepository;
    
    @Override
    public Map<String, Object> getPerformanceStats(LocalDateTime startTime, LocalDateTime endTime,
                                                  String modelCode, String appType, Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 构建查询条件
        Specification<AppLog> spec = buildSpecification(startTime, endTime, modelCode, appType, userId);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        if (logs.isEmpty()) {
            stats.put("totalCount", 0);
            stats.put("successCount", 0);
            stats.put("failedCount", 0);
            stats.put("successRate", 0.0);
            stats.put("failureRate", 0.0);
            stats.put("avgResponseTime", 0.0);
            stats.put("p50ResponseTime", 0.0);
            stats.put("p95ResponseTime", 0.0);
            stats.put("p99ResponseTime", 0.0);
            return stats;
        }
        
        // 基础统计
        long totalCount = logs.size();
        long successCount = logs.stream().filter(log -> "success".equals(log.getStatus())).count();
        long failedCount = totalCount - successCount;
        
        // 响应时间统计
        List<Integer> responseTimes = logs.stream()
                .filter(log -> log.getResponseTime() != null && "success".equals(log.getStatus()))
                .map(AppLog::getResponseTime)
                .sorted()
                .collect(Collectors.toList());
        
        double avgResponseTime = responseTimes.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        double p50 = calculatePercentile(responseTimes, 50);
        double p95 = calculatePercentile(responseTimes, 95);
        double p99 = calculatePercentile(responseTimes, 99);
        
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("successRate", totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0);
        stats.put("failureRate", totalCount > 0 ? (double) failedCount / totalCount * 100 : 0.0);
        stats.put("avgResponseTime", avgResponseTime);
        stats.put("p50ResponseTime", p50);
        stats.put("p95ResponseTime", p95);
        stats.put("p99ResponseTime", p99);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getResponseTimeStats(LocalDateTime startTime, LocalDateTime endTime,
                                                   String modelCode, String appType) {
        Map<String, Object> stats = new HashMap<>();
        
        Specification<AppLog> spec = buildSpecification(startTime, endTime, modelCode, appType, null);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        List<Integer> responseTimes = logs.stream()
                .filter(log -> log.getResponseTime() != null && "success".equals(log.getStatus()))
                .map(AppLog::getResponseTime)
                .sorted()
                .collect(Collectors.toList());
        
        if (responseTimes.isEmpty()) {
            stats.put("min", 0);
            stats.put("max", 0);
            stats.put("avg", 0.0);
            stats.put("p50", 0.0);
            stats.put("p95", 0.0);
            stats.put("p99", 0.0);
            return stats;
        }
        
        stats.put("min", responseTimes.get(0));
        stats.put("max", responseTimes.get(responseTimes.size() - 1));
        stats.put("avg", responseTimes.stream().mapToInt(Integer::intValue).average().orElse(0.0));
        stats.put("p50", calculatePercentile(responseTimes, 50));
        stats.put("p95", calculatePercentile(responseTimes, 95));
        stats.put("p99", calculatePercentile(responseTimes, 99));
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getSuccessRateStats(LocalDateTime startTime, LocalDateTime endTime,
                                                 String modelCode, String appType) {
        Map<String, Object> stats = new HashMap<>();
        
        Specification<AppLog> spec = buildSpecification(startTime, endTime, modelCode, appType, null);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        if (logs.isEmpty()) {
            stats.put("totalCount", 0);
            stats.put("successCount", 0);
            stats.put("failedCount", 0);
            stats.put("successRate", 0.0);
            stats.put("failureRate", 0.0);
            return stats;
        }
        
        long totalCount = logs.size();
        long successCount = logs.stream().filter(log -> "success".equals(log.getStatus())).count();
        long failedCount = totalCount - successCount;
        
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("successRate", (double) successCount / totalCount * 100);
        stats.put("failureRate", (double) failedCount / totalCount * 100);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getTokenUsageStats(LocalDateTime startTime, LocalDateTime endTime,
                                                 String modelCode, String appType) {
        Map<String, Object> stats = new HashMap<>();
        
        Specification<AppLog> spec = buildSpecification(startTime, endTime, modelCode, appType, null);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        long totalTokens = logs.stream()
                .filter(log -> log.getTokensTotal() != null)
                .mapToLong(AppLog::getTokensTotal)
                .sum();
        
        long avgTokens = logs.stream()
                .filter(log -> log.getTokensTotal() != null)
                .mapToLong(AppLog::getTokensTotal)
                .sum() / Math.max(logs.size(), 1);
        
        stats.put("totalTokens", totalTokens);
        stats.put("avgTokens", avgTokens);
        stats.put("requestCount", logs.size());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getCostStats(LocalDateTime startTime, LocalDateTime endTime,
                                          String modelCode, String appType, Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Specification<AppLog> spec = buildSpecification(startTime, endTime, modelCode, appType, userId);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        BigDecimal totalCost = logs.stream()
                .filter(log -> log.getCost() != null)
                .map(AppLog::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgCost = logs.isEmpty() ? BigDecimal.ZERO :
                totalCost.divide(new BigDecimal(logs.size()), 6, BigDecimal.ROUND_HALF_UP);
        
        stats.put("totalCost", totalCost);
        stats.put("avgCost", avgCost);
        stats.put("requestCount", logs.size());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getModelUsageStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        List<AppLog> logs = appLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
        
        Map<String, Long> modelCounts = logs.stream()
                .filter(log -> log.getModelCode() != null)
                .collect(Collectors.groupingBy(AppLog::getModelCode, Collectors.counting()));
        
        stats.put("modelUsage", modelCounts);
        stats.put("totalRequests", logs.size());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getAppUsageStats(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        List<AppLog> logs = appLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
        
        Map<String, Long> appCounts = logs.stream()
                .filter(log -> log.getAppType() != null)
                .collect(Collectors.groupingBy(AppLog::getAppType, Collectors.counting()));
        
        stats.put("appUsage", appCounts);
        stats.put("totalRequests", logs.size());
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getTimeSeriesData(LocalDateTime startTime, LocalDateTime endTime,
                                                String interval, String metric,
                                                String modelCode, String appType) {
        Map<String, Object> result = new HashMap<>();
        
        Specification<AppLog> spec = buildSpecification(startTime, endTime, modelCode, appType, null);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        // 按时间间隔分组
        Map<String, List<AppLog>> grouped = groupByInterval(logs, interval);
        
        List<Map<String, Object>> dataPoints = new ArrayList<>();
        for (Map.Entry<String, List<AppLog>> entry : grouped.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("time", entry.getKey());
            
            List<AppLog> groupLogs = entry.getValue();
            switch (metric) {
                case "RESPONSE_TIME":
                    double avgTime = groupLogs.stream()
                            .filter(log -> log.getResponseTime() != null && "success".equals(log.getStatus()))
                            .mapToInt(AppLog::getResponseTime)
                            .average()
                            .orElse(0.0);
                    point.put("value", avgTime);
                    break;
                case "SUCCESS_RATE":
                    long successCount = groupLogs.stream().filter(log -> "success".equals(log.getStatus())).count();
                    double rate = groupLogs.isEmpty() ? 0 : (double) successCount / groupLogs.size() * 100;
                    point.put("value", rate);
                    break;
                case "TOKEN_USAGE":
                    long totalTokens = groupLogs.stream()
                            .filter(log -> log.getTokensTotal() != null)
                            .mapToLong(AppLog::getTokensTotal)
                            .sum();
                    point.put("value", totalTokens);
                    break;
                case "COST":
                    BigDecimal totalCost = groupLogs.stream()
                            .filter(log -> log.getCost() != null)
                            .map(AppLog::getCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    point.put("value", totalCost);
                    break;
            }
            dataPoints.add(point);
        }
        
        result.put("data", dataPoints);
        result.put("metric", metric);
        result.put("interval", interval);
        
        return result;
    }
    
    /**
     * 构建查询条件
     */
    private Specification<AppLog> buildSpecification(LocalDateTime startTime, LocalDateTime endTime,
                                                      String modelCode, String appType, Long userId) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // 时间范围
            predicates.add(cb.between(root.get("timestamp"), startTime, endTime));
            
            // 模型代码
            if (modelCode != null && !modelCode.isEmpty()) {
                predicates.add(cb.equal(root.get("modelCode"), modelCode));
            }
            
            // 应用类型
            if (appType != null && !appType.isEmpty()) {
                predicates.add(cb.equal(root.get("appType"), appType));
            }
            
            // 用户ID
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    /**
     * 计算百分位数
     */
    private double calculatePercentile(List<Integer> values, int percentile) {
        if (values.isEmpty()) {
            return 0.0;
        }
        int index = (int) Math.ceil(values.size() * percentile / 100.0) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }
    
    /**
     * 按时间间隔分组
     */
    private Map<String, List<AppLog>> groupByInterval(List<AppLog> logs, String interval) {
        Map<String, List<AppLog>> grouped = new LinkedHashMap<>();
        
        for (AppLog log : logs) {
            String key = formatTimeKey(log.getTimestamp(), interval);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(log);
        }
        
        return grouped;
    }
    
    /**
     * 格式化时间键
     */
    private String formatTimeKey(LocalDateTime time, String interval) {
        switch (interval) {
            case "HOUR":
                return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"));
            case "DAY":
                return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "WEEK":
                return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-'W'ww"));
            case "MONTH":
                return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            default:
                return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
}
