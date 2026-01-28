package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.AlertRecord;
import com.sinosoft.testdesign.entity.AlertRule;
import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.repository.AlertRecordRepository;
import com.sinosoft.testdesign.repository.AlertRuleRepository;
import com.sinosoft.testdesign.repository.AppLogRepository;
import com.sinosoft.testdesign.service.AlertService;
import com.sinosoft.testdesign.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 告警服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {
    
    private final AlertRuleRepository ruleRepository;
    private final AlertRecordRepository recordRepository;
    private final AppLogRepository appLogRepository;
    private final NotificationService notificationService;
    
    @Override
    @Transactional
    public AlertRule createAlertRule(AlertRule rule) {
        // 检查规则代码是否已存在
        Optional<AlertRule> existing = ruleRepository.findByRuleCode(rule.getRuleCode());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("规则代码已存在: " + rule.getRuleCode());
        }
        
        return ruleRepository.save(rule);
    }
    
    @Override
    @Transactional
    public AlertRule updateAlertRule(AlertRule rule) {
        if (rule.getId() == null) {
            throw new IllegalArgumentException("告警规则ID不能为空");
        }
        
        Optional<AlertRule> existing = ruleRepository.findById(rule.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("告警规则不存在: " + rule.getId());
        }
        
        return ruleRepository.save(rule);
    }
    
    @Override
    public Optional<AlertRule> findRuleById(Long id) {
        return ruleRepository.findById(id);
    }
    
    @Override
    public Optional<AlertRule> findRuleByCode(String ruleCode) {
        return ruleRepository.findByRuleCode(ruleCode);
    }
    
    @Override
    public List<AlertRule> findAllEnabledRules() {
        return ruleRepository.findByIsEnabledTrue();
    }
    
    @Override
    public List<AlertRule> findAllRules() {
        return ruleRepository.findAll();
    }
    
    @Override
    @Transactional
    public void deleteRuleById(Long id) {
        ruleRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public AlertRule toggleRuleEnabled(Long id, Boolean isEnabled) {
        Optional<AlertRule> rule = ruleRepository.findById(id);
        if (rule.isEmpty()) {
            throw new IllegalArgumentException("告警规则不存在: " + id);
        }
        
        AlertRule alertRule = rule.get();
        alertRule.setIsEnabled(isEnabled);
        return ruleRepository.save(alertRule);
    }
    
    @Override
    @Transactional
    public void checkAlertRules() {
        List<AlertRule> enabledRules = ruleRepository.findByIsEnabledTrue();
        
        for (AlertRule rule : enabledRules) {
            try {
                checkSingleRule(rule);
            } catch (Exception e) {
                log.error("检查告警规则失败: {}", rule.getRuleCode(), e);
            }
        }
    }
    
    /**
     * 检查单个告警规则
     */
    private void checkSingleRule(AlertRule rule) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusSeconds(rule.getTimeWindow());
        
        // 构建查询条件
        Specification<AppLog> spec = buildSpecification(startTime, endTime, rule);
        List<AppLog> logs = appLogRepository.findAll(spec);
        
        if (logs.isEmpty()) {
            return;
        }
        
        // 根据规则类型计算当前值
        BigDecimal currentValue = calculateCurrentValue(logs, rule.getRuleType());
        
        // 检查是否触发告警
        boolean shouldAlert = checkThreshold(currentValue, rule.getThresholdValue(), rule.getAlertCondition());
        
        if (shouldAlert) {
            // 检查是否已有未解决的相同告警
            List<AlertRecord> existingAlerts = recordRepository.findByRuleCodeAndIsResolvedFalse(rule.getRuleCode());
            if (existingAlerts.isEmpty()) {
                // 创建告警记录
                createAlertRecord(rule, currentValue);
            }
        }
    }
    
    /**
     * 构建查询条件
     */
    private Specification<AppLog> buildSpecification(LocalDateTime startTime, LocalDateTime endTime, AlertRule rule) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            // 时间范围
            predicates.add(cb.between(root.get("timestamp"), startTime, endTime));
            
            // 目标范围
            if ("MODEL".equals(rule.getTargetScope()) && rule.getTargetValue() != null) {
                predicates.add(cb.equal(root.get("modelCode"), rule.getTargetValue()));
            } else if ("APP".equals(rule.getTargetScope()) && rule.getTargetValue() != null) {
                predicates.add(cb.equal(root.get("appType"), rule.getTargetValue()));
            } else if ("USER".equals(rule.getTargetScope()) && rule.getTargetValue() != null) {
                predicates.add(cb.equal(root.get("userId"), Long.parseLong(rule.getTargetValue())));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    /**
     * 计算当前值
     */
    private BigDecimal calculateCurrentValue(List<AppLog> logs, String ruleType) {
        switch (ruleType) {
            case "FAILURE_RATE":
                long failedCount = logs.stream().filter(log -> "failed".equals(log.getStatus())).count();
                return BigDecimal.valueOf((double) failedCount / logs.size() * 100);
            case "RESPONSE_TIME":
                double avgTime = logs.stream()
                        .filter(log -> log.getResponseTime() != null && "success".equals(log.getStatus()))
                        .mapToInt(AppLog::getResponseTime)
                        .average()
                        .orElse(0.0);
                return BigDecimal.valueOf(avgTime);
            case "COST":
                BigDecimal totalCost = logs.stream()
                        .filter(log -> log.getCost() != null)
                        .map(AppLog::getCost)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return totalCost;
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * 检查阈值
     */
    private boolean checkThreshold(BigDecimal currentValue, BigDecimal thresholdValue, String condition) {
        int comparison = currentValue.compareTo(thresholdValue);
        
        switch (condition) {
            case "GT":
                return comparison > 0;
            case "GTE":
                return comparison >= 0;
            case "LT":
                return comparison < 0;
            case "LTE":
                return comparison <= 0;
            case "EQ":
                return comparison == 0;
            default:
                return false;
        }
    }
    
    /**
     * 创建告警记录
     */
    private void createAlertRecord(AlertRule rule, BigDecimal currentValue) {
        String alertLevel = determineAlertLevel(rule.getRuleType(), currentValue, rule.getThresholdValue());
        
        AlertRecord record = AlertRecord.builder()
                .ruleId(rule.getId())
                .ruleCode(rule.getRuleCode())
                .alertLevel(alertLevel)
                .alertTitle(buildAlertTitle(rule))
                .alertMessage(buildAlertMessage(rule, currentValue))
                .currentValue(currentValue)
                .thresholdValue(rule.getThresholdValue())
                .targetScope(rule.getTargetScope())
                .targetValue(rule.getTargetValue())
                .alertTime(LocalDateTime.now())
                .isResolved(false)
                .notificationSent(false)
                .build();
        
        recordRepository.save(record);
        
        // 发送通知
        try {
            boolean sent = notificationService.sendAlertNotification(record);
            if (sent) {
                log.info("告警通知发送成功: alertId={}, ruleCode={}", record.getId(), record.getRuleCode());
            } else {
                log.warn("告警通知发送失败: alertId={}, ruleCode={}", record.getId(), record.getRuleCode());
            }
        } catch (Exception e) {
            log.error("发送告警通知异常: alertId={}, ruleCode={}", record.getId(), record.getRuleCode(), e);
        }
        
        log.warn("告警触发: {}", record.getAlertTitle());
    }
    
    /**
     * 确定告警级别
     */
    private String determineAlertLevel(String ruleType, BigDecimal currentValue, BigDecimal thresholdValue) {
        // 根据超出阈值的程度确定告警级别
        BigDecimal ratio = currentValue.divide(thresholdValue, 2, BigDecimal.ROUND_HALF_UP);
        
        if (ratio.compareTo(new BigDecimal("2.0")) >= 0) {
            return "CRITICAL";
        } else if (ratio.compareTo(new BigDecimal("1.5")) >= 0) {
            return "ERROR";
        } else if (ratio.compareTo(new BigDecimal("1.2")) >= 0) {
            return "WARNING";
        } else {
            return "INFO";
        }
    }
    
    /**
     * 构建告警标题
     */
    private String buildAlertTitle(AlertRule rule) {
        return String.format("%s告警", rule.getRuleName());
    }
    
    /**
     * 构建告警消息
     */
    private String buildAlertMessage(AlertRule rule, BigDecimal currentValue) {
        return String.format("%s当前值为%s%s，超过阈值%s%s",
                rule.getRuleName(),
                currentValue,
                rule.getThresholdUnit(),
                rule.getThresholdValue(),
                rule.getThresholdUnit());
    }
    
    @Override
    @Transactional
    public AlertRecord createAlertRecord(AlertRecord record) {
        if (record.getAlertTime() == null) {
            record.setAlertTime(LocalDateTime.now());
        }
        return recordRepository.save(record);
    }
    
    @Override
    public Page<AlertRecord> findAlertRecords(Pageable pageable) {
        return recordRepository.findAll(pageable);
    }
    
    @Override
    public List<AlertRecord> findUnresolvedAlerts() {
        return recordRepository.findByIsResolvedFalseOrderByAlertTimeDesc();
    }
    
    @Override
    @Transactional
    public AlertRecord resolveAlert(Long alertId, Long resolvedBy, String resolvedNote) {
        Optional<AlertRecord> record = recordRepository.findById(alertId);
        if (record.isEmpty()) {
            throw new IllegalArgumentException("告警记录不存在: " + alertId);
        }
        
        AlertRecord alertRecord = record.get();
        alertRecord.setIsResolved(true);
        alertRecord.setResolvedBy(resolvedBy);
        alertRecord.setResolvedAt(LocalDateTime.now());
        alertRecord.setResolvedNote(resolvedNote);
        
        return recordRepository.save(alertRecord);
    }
}
