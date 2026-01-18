package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 测试执行统计DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestExecutionStatisticsDTO {
    
    /**
     * 任务统计
     */
    @Data
    public static class TaskStatistics {
        /**
         * 总任务数
         */
        private Long totalTasks;
        
        /**
         * 按状态统计
         */
        private Map<String, Long> statusCount;
        
        /**
         * 按类型统计
         */
        private Map<String, Long> typeCount;
        
        /**
         * 待处理任务数
         */
        private Long pendingCount;
        
        /**
         * 处理中任务数
         */
        private Long processingCount;
        
        /**
         * 成功任务数
         */
        private Long successCount;
        
        /**
         * 失败任务数
         */
        private Long failedCount;
    }
    
    /**
     * 执行记录统计
     */
    @Data
    public static class RecordStatistics {
        /**
         * 总记录数
         */
        private Long totalRecords;
        
        /**
         * 按状态统计
         */
        private Map<String, Long> statusCount;
        
        /**
         * 按类型统计
         */
        private Map<String, Long> typeCount;
        
        /**
         * 成功记录数
         */
        private Long successCount;
        
        /**
         * 失败记录数
         */
        private Long failedCount;
        
        /**
         * 跳过记录数
         */
        private Long skippedCount;
        
        /**
         * 成功率（百分比）
         */
        private Double successRate;
        
        /**
         * 失败率（百分比）
         */
        private Double failureRate;
        
        /**
         * 平均执行耗时（毫秒）
         */
        private Double avgDuration;
        
        /**
         * 总执行耗时（毫秒）
         */
        private Long totalDuration;
    }
    
    /**
     * 趋势统计
     */
    @Data
    public static class TrendStatistics {
        /**
         * 日期
         */
        private String date;
        
        /**
         * 任务数
         */
        private Long taskCount;
        
        /**
         * 执行记录数
         */
        private Long recordCount;
        
        /**
         * 成功数
         */
        private Long successCount;
        
        /**
         * 失败数
         */
        private Long failedCount;
        
        /**
         * 成功率
         */
        private Double successRate;
    }
    
    /**
     * 任务统计
     */
    private TaskStatistics taskStatistics;
    
    /**
     * 执行记录统计
     */
    private RecordStatistics recordStatistics;
    
    /**
     * 趋势统计（最近7天）
     */
    private List<TrendStatistics> trendStatistics;
}

