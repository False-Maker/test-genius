package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.AlertRecord;
import com.sinosoft.testdesign.entity.AlertRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 告警服务接口
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface AlertService {
    
    /**
     * 创建告警规则
     */
    AlertRule createAlertRule(AlertRule rule);
    
    /**
     * 更新告警规则
     */
    AlertRule updateAlertRule(AlertRule rule);
    
    /**
     * 根据ID查询告警规则
     */
    Optional<AlertRule> findRuleById(Long id);
    
    /**
     * 根据规则代码查询告警规则
     */
    Optional<AlertRule> findRuleByCode(String ruleCode);
    
    /**
     * 查询所有启用的告警规则
     */
    List<AlertRule> findAllEnabledRules();
    
    /**
     * 查询所有告警规则
     */
    List<AlertRule> findAllRules();
    
    /**
     * 删除告警规则
     */
    void deleteRuleById(Long id);
    
    /**
     * 启用/禁用告警规则
     */
    AlertRule toggleRuleEnabled(Long id, Boolean isEnabled);
    
    /**
     * 检查告警规则（触发告警）
     */
    void checkAlertRules();
    
    /**
     * 创建告警记录
     */
    AlertRecord createAlertRecord(AlertRecord record);
    
    /**
     * 查询告警记录
     */
    Page<AlertRecord> findAlertRecords(Pageable pageable);
    
    /**
     * 查询未解决的告警记录
     */
    List<AlertRecord> findUnresolvedAlerts();
    
    /**
     * 解决告警
     */
    AlertRecord resolveAlert(Long alertId, Long resolvedBy, String resolvedNote);
}
