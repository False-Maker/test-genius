package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AlertRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警记录Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long>, JpaSpecificationExecutor<AlertRecord> {
    
    /**
     * 根据规则ID查询告警记录
     */
    List<AlertRecord> findByRuleIdOrderByAlertTimeDesc(Long ruleId);
    
    /**
     * 根据规则代码查询告警记录
     */
    List<AlertRecord> findByRuleCodeOrderByAlertTimeDesc(String ruleCode);
    
    /**
     * 根据告警级别查询告警记录
     */
    List<AlertRecord> findByAlertLevelOrderByAlertTimeDesc(String alertLevel);
    
    /**
     * 根据是否已解决查询告警记录
     */
    List<AlertRecord> findByIsResolvedOrderByAlertTimeDesc(Boolean isResolved);
    
    /**
     * 根据时间范围查询告警记录
     */
    List<AlertRecord> findByAlertTimeBetweenOrderByAlertTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询未解决的告警记录
     */
    List<AlertRecord> findByIsResolvedFalseOrderByAlertTimeDesc();
    
    /**
     * 根据规则代码和是否已解决查询告警记录
     */
    List<AlertRecord> findByRuleCodeAndIsResolvedFalse(String ruleCode);
}
