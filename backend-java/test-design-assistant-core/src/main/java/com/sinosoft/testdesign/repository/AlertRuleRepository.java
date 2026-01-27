package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 告警规则Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long>, JpaSpecificationExecutor<AlertRule> {
    
    /**
     * 根据规则代码查询
     */
    Optional<AlertRule> findByRuleCode(String ruleCode);
    
    /**
     * 查询所有启用的规则
     */
    List<AlertRule> findByIsEnabledTrue();
    
    /**
     * 根据规则类型查询启用的规则
     */
    List<AlertRule> findByRuleTypeAndIsEnabledTrue(String ruleType);
}
