package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.TestExecutionStatisticsDTO;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import com.sinosoft.testdesign.repository.TestExecutionTaskRepository;
import com.sinosoft.testdesign.repository.TestExecutionRecordRepository;
import com.sinosoft.testdesign.service.TestExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试执行服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionServiceImpl implements TestExecutionService {
    
    private final TestExecutionTaskRepository taskRepository;
    private final TestExecutionRecordRepository recordRepository;
    
    private static final String TASK_CODE_PREFIX = "TASK";
    private static final String RECORD_CODE_PREFIX = "REC";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public TestExecutionTask createExecutionTask(TestExecutionTask task) {
        log.info("创建执行任务: {}", task.getTaskName());
        
        // 数据验证
        if (!StringUtils.hasText(task.getTaskName())) {
            throw new BusinessException("任务名称不能为空");
        }
        if (!StringUtils.hasText(task.getTaskType())) {
            throw new BusinessException("任务类型不能为空");
        }
        
        // 自动生成任务编码（如果未提供）
        if (!StringUtils.hasText(task.getTaskCode())) {
            task.setTaskCode(generateTaskCode());
        } else {
            // 检查编码是否已存在
            if (taskRepository.findByTaskCode(task.getTaskCode()).isPresent()) {
                throw new BusinessException("任务编码已存在: " + task.getTaskCode());
            }
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(task.getTaskStatus())) {
            task.setTaskStatus("PENDING");
        }
        if (task.getProgress() == null) {
            task.setProgress(0);
        }
        
        log.info("创建执行任务成功，编码: {}", task.getTaskCode());
        return taskRepository.save(task);
    }
    
    @Override
    @Transactional
    public TestExecutionTask updateExecutionTask(Long id, TestExecutionTask task) {
        log.info("更新执行任务: {}", id);
        
        TestExecutionTask existing = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("执行任务不存在"));
        
        // 不允许修改任务编码
        if (StringUtils.hasText(task.getTaskCode()) && 
            !task.getTaskCode().equals(existing.getTaskCode())) {
            throw new BusinessException("任务编码不允许修改");
        }
        
        // 更新字段
        if (StringUtils.hasText(task.getTaskName())) {
            existing.setTaskName(task.getTaskName());
        }
        if (StringUtils.hasText(task.getTaskType())) {
            existing.setTaskType(task.getTaskType());
        }
        if (task.getRequirementId() != null) {
            existing.setRequirementId(task.getRequirementId());
        }
        if (task.getCaseId() != null) {
            existing.setCaseId(task.getCaseId());
        }
        if (task.getCaseSuiteId() != null) {
            existing.setCaseSuiteId(task.getCaseSuiteId());
        }
        if (StringUtils.hasText(task.getScriptType())) {
            existing.setScriptType(task.getScriptType());
        }
        if (StringUtils.hasText(task.getScriptContent())) {
            existing.setScriptContent(task.getScriptContent());
        }
        if (StringUtils.hasText(task.getScriptLanguage())) {
            existing.setScriptLanguage(task.getScriptLanguage());
        }
        if (StringUtils.hasText(task.getPageCodeUrl())) {
            existing.setPageCodeUrl(task.getPageCodeUrl());
        }
        if (StringUtils.hasText(task.getNaturalLanguageDesc())) {
            existing.setNaturalLanguageDesc(task.getNaturalLanguageDesc());
        }
        if (StringUtils.hasText(task.getErrorLog())) {
            existing.setErrorLog(task.getErrorLog());
        }
        if (StringUtils.hasText(task.getExecutionConfig())) {
            existing.setExecutionConfig(task.getExecutionConfig());
        }
        if (StringUtils.hasText(task.getTaskStatus())) {
            existing.setTaskStatus(task.getTaskStatus());
        }
        if (task.getProgress() != null) {
            existing.setProgress(task.getProgress());
        }
        if (task.getSuccessCount() != null) {
            existing.setSuccessCount(task.getSuccessCount());
        }
        if (task.getFailCount() != null) {
            existing.setFailCount(task.getFailCount());
        }
        if (StringUtils.hasText(task.getResultData())) {
            existing.setResultData(task.getResultData());
        }
        if (StringUtils.hasText(task.getErrorMessage())) {
            existing.setErrorMessage(task.getErrorMessage());
        }
        
        log.info("更新执行任务成功，编码: {}", existing.getTaskCode());
        return taskRepository.save(existing);
    }
    
    @Override
    public TestExecutionTask getExecutionTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("执行任务不存在"));
    }
    
    @Override
    public TestExecutionTask getExecutionTaskByCode(String taskCode) {
        return taskRepository.findByTaskCode(taskCode)
                .orElseThrow(() -> new BusinessException("执行任务不存在: " + taskCode));
    }
    
    @Override
    public Page<TestExecutionTask> getExecutionTaskList(Pageable pageable, String taskName, 
            String taskStatus, String taskType) {
        return taskRepository.findWithFilters(taskName, taskStatus, taskType, pageable);
    }
    
    @Override
    @Transactional
    public TestExecutionTask updateTaskStatus(String taskCode, String status) {
        log.info("更新任务状态: {} -> {}", taskCode, status);
        
        TestExecutionTask task = taskRepository.findByTaskCode(taskCode)
                .orElseThrow(() -> new BusinessException("执行任务不存在: " + taskCode));
        
        task.setTaskStatus(status);
        
        // 如果状态为完成，设置完成时间
        if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
            task.setFinishTime(java.time.LocalDateTime.now());
        }
        
        log.info("更新任务状态成功，编码: {}, 新状态: {}", taskCode, status);
        return taskRepository.save(task);
    }
    
    @Override
    @Transactional
    public void deleteExecutionTask(Long id) {
        log.info("删除执行任务: {}", id);
        
        TestExecutionTask task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("执行任务不存在"));
        
        // 检查是否存在关联的执行记录
        List<TestExecutionRecord> records = recordRepository.findByTaskId(id);
        if (!records.isEmpty()) {
            throw new BusinessException("存在关联的执行记录，无法删除");
        }
        
        taskRepository.delete(task);
        log.info("删除执行任务成功，编码: {}", task.getTaskCode());
    }
    
    @Override
    @Transactional
    public TestExecutionRecord createExecutionRecord(TestExecutionRecord record) {
        log.info("创建执行记录，任务ID: {}", record.getTaskId());
        
        // 数据验证
        if (record.getTaskId() == null) {
            throw new BusinessException("任务ID不能为空");
        }
        if (!StringUtils.hasText(record.getExecutionType())) {
            throw new BusinessException("执行类型不能为空");
        }
        
        // 验证任务是否存在
        taskRepository.findById(record.getTaskId())
                .orElseThrow(() -> new BusinessException("执行任务不存在"));
        
        // 自动生成记录编码（如果未提供）
        if (!StringUtils.hasText(record.getRecordCode())) {
            record.setRecordCode(generateRecordCode());
        } else {
            // 检查编码是否已存在
            if (recordRepository.findByRecordCode(record.getRecordCode()).isPresent()) {
                throw new BusinessException("记录编码已存在: " + record.getRecordCode());
            }
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(record.getExecutionStatus())) {
            record.setExecutionStatus("PENDING");
        }
        if (record.getExecutionTime() == null) {
            record.setExecutionTime(java.time.LocalDateTime.now());
        }
        
        log.info("创建执行记录成功，编码: {}", record.getRecordCode());
        return recordRepository.save(record);
    }
    
    @Override
    public TestExecutionRecord getExecutionRecordById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("执行记录不存在"));
    }
    
    @Override
    public TestExecutionRecord getExecutionRecordByCode(String recordCode) {
        return recordRepository.findByRecordCode(recordCode)
                .orElseThrow(() -> new BusinessException("执行记录不存在: " + recordCode));
    }
    
    @Override
    public List<TestExecutionRecord> getExecutionRecordsByTaskId(Long taskId) {
        return recordRepository.findByTaskId(taskId);
    }
    
    @Override
    public Page<TestExecutionRecord> getExecutionRecordList(Pageable pageable, Long taskId, 
            Long caseId, String executionStatus) {
        return recordRepository.findWithFilters(taskId, caseId, executionStatus, pageable);
    }
    
    @Override
    @Transactional
    public TestExecutionRecord updateExecutionRecordStatus(String recordCode, String status) {
        log.info("更新执行记录状态: {} -> {}", recordCode, status);
        
        TestExecutionRecord record = recordRepository.findByRecordCode(recordCode)
                .orElseThrow(() -> new BusinessException("执行记录不存在: " + recordCode));
        
        record.setExecutionStatus(status);
        
        // 如果状态为完成，设置完成时间
        if ("SUCCESS".equals(status) || "FAILED".equals(status) || "SKIPPED".equals(status)) {
            record.setFinishTime(java.time.LocalDateTime.now());
        }
        
        log.info("更新执行记录状态成功，编码: {}, 新状态: {}", recordCode, status);
        return recordRepository.save(record);
    }
    
    /**
     * 生成任务编码
     * 格式：TASK-YYYYMMDD-序号（如 TASK-20240117-001）
     */
    private String generateTaskCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = TASK_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天前缀的任务，避免全表扫描
        List<TestExecutionTask> todayTasks = taskRepository.findByTaskCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestExecutionTask task : todayTasks) {
            String code = task.getTaskCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                    log.warn("任务编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        String taskCode = prefix + String.format("%03d", newSequence);
        log.debug("生成任务编码: {}", taskCode);
        return taskCode;
    }
    
    /**
     * 生成记录编码
     * 格式：REC-YYYYMMDD-序号（如 REC-20240117-001）
     */
    private String generateRecordCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = RECORD_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天前缀的记录，避免全表扫描
        List<TestExecutionRecord> todayRecords = recordRepository.findByRecordCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestExecutionRecord record : todayRecords) {
            String code = record.getRecordCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                    log.warn("记录编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        String recordCode = prefix + String.format("%03d", newSequence);
        log.debug("生成记录编码: {}", recordCode);
        return recordCode;
    }
    
    @Override
    @Transactional
    public TestExecutionTask updateTaskProgress(String taskCode, Integer progress) {
        log.info("更新任务进度: {} -> {}", taskCode, progress);
        
        TestExecutionTask task = taskRepository.findByTaskCode(taskCode)
                .orElseThrow(() -> new BusinessException("执行任务不存在: " + taskCode));
        
        // 验证进度值
        if (progress < 0 || progress > 100) {
            throw new BusinessException("进度值必须在0-100之间");
        }
        
        task.setProgress(progress);
        
        log.info("更新任务进度成功，编码: {}, 新进度: {}", taskCode, progress);
        return taskRepository.save(task);
    }
    
    @Override
    public TestExecutionStatisticsDTO getExecutionStatistics(
            Long requirementId, Long caseId, String startDate, String endDate) {
        log.info("获取执行统计信息，需求ID: {}, 用例ID: {}, 开始日期: {}, 结束日期: {}", 
                requirementId, caseId, startDate, endDate);
        
        TestExecutionStatisticsDTO statistics = new TestExecutionStatisticsDTO();
        
        // 构建查询条件
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (StringUtils.hasText(startDate)) {
            startDateTime = LocalDate.parse(startDate).atStartOfDay();
        }
        if (StringUtils.hasText(endDate)) {
            endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        }
        
        // 任务统计
        TestExecutionStatisticsDTO.TaskStatistics taskStats = getTaskStatistics(requirementId, caseId, startDateTime, endDateTime);
        statistics.setTaskStatistics(taskStats);
        
        // 执行记录统计
        TestExecutionStatisticsDTO.RecordStatistics recordStats = getRecordStatistics(requirementId, caseId, startDateTime, endDateTime);
        statistics.setRecordStatistics(recordStats);
        
        // 趋势统计（最近7天）
        List<TestExecutionStatisticsDTO.TrendStatistics> trendStats = getTrendStatistics(requirementId, caseId, 7);
        statistics.setTrendStatistics(trendStats);
        
        return statistics;
    }
    
    /**
     * 获取任务统计
     */
    private TestExecutionStatisticsDTO.TaskStatistics getTaskStatistics(
            Long requirementId, Long caseId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        TestExecutionStatisticsDTO.TaskStatistics stats = new TestExecutionStatisticsDTO.TaskStatistics();
        
        // 查询所有任务
        List<TestExecutionTask> tasks = taskRepository.findAll();
        
        // 过滤条件
        if (requirementId != null) {
            tasks = tasks.stream()
                    .filter(t -> requirementId.equals(t.getRequirementId()))
                    .collect(Collectors.toList());
        }
        if (caseId != null) {
            tasks = tasks.stream()
                    .filter(t -> caseId.equals(t.getCaseId()))
                    .collect(Collectors.toList());
        }
        if (startDateTime != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getCreateTime() != null && !t.getCreateTime().isBefore(startDateTime))
                    .collect(Collectors.toList());
        }
        if (endDateTime != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getCreateTime() != null && !t.getCreateTime().isAfter(endDateTime))
                    .collect(Collectors.toList());
        }
        
        stats.setTotalTasks((long) tasks.size());
        
        // 按状态统计
        Map<String, Long> statusCount = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTaskStatus() != null ? t.getTaskStatus() : "UNKNOWN",
                        Collectors.counting()));
        stats.setStatusCount(statusCount);
        
        // 按类型统计
        Map<String, Long> typeCount = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTaskType() != null ? t.getTaskType() : "UNKNOWN",
                        Collectors.counting()));
        stats.setTypeCount(typeCount);
        
        // 各状态数量
        stats.setPendingCount(statusCount.getOrDefault("PENDING", 0L));
        stats.setProcessingCount(statusCount.getOrDefault("PROCESSING", 0L));
        stats.setSuccessCount(statusCount.getOrDefault("SUCCESS", 0L));
        stats.setFailedCount(statusCount.getOrDefault("FAILED", 0L));
        
        return stats;
    }
    
    /**
     * 获取执行记录统计
     */
    private TestExecutionStatisticsDTO.RecordStatistics getRecordStatistics(
            Long requirementId, Long caseId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        TestExecutionStatisticsDTO.RecordStatistics stats = new TestExecutionStatisticsDTO.RecordStatistics();
        
        // 查询所有记录
        List<TestExecutionRecord> records = recordRepository.findAll();
        
        // 如果指定了用例ID，直接过滤
        if (caseId != null) {
            records = records.stream()
                    .filter(r -> caseId.equals(r.getCaseId()))
                    .collect(Collectors.toList());
        }
        
        // 如果指定了需求ID，需要通过任务关联
        if (requirementId != null) {
            List<Long> taskIds = taskRepository.findByRequirementId(requirementId).stream()
                    .map(TestExecutionTask::getId)
                    .collect(Collectors.toList());
            records = records.stream()
                    .filter(r -> taskIds.contains(r.getTaskId()))
                    .collect(Collectors.toList());
        }
        
        // 时间过滤
        if (startDateTime != null) {
            records = records.stream()
                    .filter(r -> r.getExecutionTime() != null && !r.getExecutionTime().isBefore(startDateTime))
                    .collect(Collectors.toList());
        }
        if (endDateTime != null) {
            records = records.stream()
                    .filter(r -> r.getExecutionTime() != null && !r.getExecutionTime().isAfter(endDateTime))
                    .collect(Collectors.toList());
        }
        
        stats.setTotalRecords((long) records.size());
        
        // 按状态统计
        Map<String, Long> statusCount = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getExecutionStatus() != null ? r.getExecutionStatus() : "UNKNOWN",
                        Collectors.counting()));
        stats.setStatusCount(statusCount);
        
        // 按类型统计
        Map<String, Long> typeCount = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getExecutionType() != null ? r.getExecutionType() : "UNKNOWN",
                        Collectors.counting()));
        stats.setTypeCount(typeCount);
        
        // 各状态数量
        stats.setSuccessCount(statusCount.getOrDefault("SUCCESS", 0L));
        stats.setFailedCount(statusCount.getOrDefault("FAILED", 0L));
        stats.setSkippedCount(statusCount.getOrDefault("SKIPPED", 0L));
        
        // 计算成功率
        long total = records.size();
        if (total > 0) {
            long success = stats.getSuccessCount();
            stats.setSuccessRate((double) success / total * 100);
            stats.setFailureRate((double) stats.getFailedCount() / total * 100);
        } else {
            stats.setSuccessRate(0.0);
            stats.setFailureRate(0.0);
        }
        
        // 计算平均耗时
        OptionalDouble avgDuration = records.stream()
                .filter(r -> r.getExecutionDuration() != null)
                .mapToInt(TestExecutionRecord::getExecutionDuration)
                .average();
        stats.setAvgDuration(avgDuration.isPresent() ? avgDuration.getAsDouble() : 0.0);
        
        // 计算总耗时
        long totalDuration = records.stream()
                .filter(r -> r.getExecutionDuration() != null)
                .mapToInt(TestExecutionRecord::getExecutionDuration)
                .sum();
        stats.setTotalDuration(totalDuration);
        
        return stats;
    }
    
    /**
     * 获取趋势统计
     */
    private List<TestExecutionStatisticsDTO.TrendStatistics> getTrendStatistics(
            Long requirementId, Long caseId, int days) {
        List<TestExecutionStatisticsDTO.TrendStatistics> trendList = new ArrayList<>();
        
        LocalDate endDate = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(23, 59, 59);
            
            TestExecutionStatisticsDTO.TrendStatistics trend = new TestExecutionStatisticsDTO.TrendStatistics();
            trend.setDate(date.format(DATE_FORMATTER));
            
            // 查询当天的任务
            List<TestExecutionTask> tasks = taskRepository.findAll().stream()
                    .filter(t -> {
                        if (t.getCreateTime() == null) return false;
                        LocalDateTime createTime = t.getCreateTime();
                        return !createTime.isBefore(startDateTime) && !createTime.isAfter(endDateTime);
                    })
                    .collect(Collectors.toList());
            
            // 过滤条件
            if (requirementId != null) {
                tasks = tasks.stream()
                        .filter(t -> requirementId.equals(t.getRequirementId()))
                        .collect(Collectors.toList());
            }
            
            trend.setTaskCount((long) tasks.size());
            
            // 查询当天的执行记录
            List<TestExecutionRecord> records = recordRepository.findAll().stream()
                    .filter(r -> {
                        if (r.getExecutionTime() == null) return false;
                        LocalDateTime execTime = r.getExecutionTime();
                        return !execTime.isBefore(startDateTime) && !execTime.isAfter(endDateTime);
                    })
                    .collect(Collectors.toList());
            
            // 过滤条件
            if (caseId != null) {
                records = records.stream()
                        .filter(r -> caseId.equals(r.getCaseId()))
                        .collect(Collectors.toList());
            }
            if (requirementId != null) {
                List<Long> taskIds = tasks.stream()
                        .map(TestExecutionTask::getId)
                        .collect(Collectors.toList());
                records = records.stream()
                        .filter(r -> taskIds.contains(r.getTaskId()))
                        .collect(Collectors.toList());
            }
            
            trend.setRecordCount((long) records.size());
            
            // 统计成功和失败
            long success = records.stream()
                    .filter(r -> "SUCCESS".equals(r.getExecutionStatus()))
                    .count();
            long failed = records.stream()
                    .filter(r -> "FAILED".equals(r.getExecutionStatus()))
                    .count();
            
            trend.setSuccessCount(success);
            trend.setFailedCount(failed);
            
            // 计算成功率
            if (records.size() > 0) {
                trend.setSuccessRate((double) success / records.size() * 100);
            } else {
                trend.setSuccessRate(0.0);
            }
            
            trendList.add(trend);
        }
        
        return trendList;
    }
}

