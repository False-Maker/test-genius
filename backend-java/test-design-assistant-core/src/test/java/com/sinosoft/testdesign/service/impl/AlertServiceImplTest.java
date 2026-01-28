package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.AlertRecord;
import com.sinosoft.testdesign.entity.AlertRule;
import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.repository.AlertRecordRepository;
import com.sinosoft.testdesign.repository.AlertRuleRepository;
import com.sinosoft.testdesign.repository.AppLogRepository;
import com.sinosoft.testdesign.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 告警服务单元测试（第四阶段 4.1）
 * 覆盖告警规则 CRUD、启用切换、规则检查等核心流程。
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("告警服务测试")
class AlertServiceImplTest {

    @Mock
    private AlertRuleRepository ruleRepository;

    @Mock
    private AlertRecordRepository recordRepository;

    @Mock
    private AppLogRepository appLogRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertServiceImpl alertService;

    private AlertRule rule;

    @BeforeEach
    void setUp() {
        rule = AlertRule.builder()
                .id(1L)
                .ruleCode("RULE-001")
                .ruleName("失败率告警")
                .ruleType("FAILURE_RATE")
                .alertCondition("GT")
                .thresholdValue(new BigDecimal("5.0"))
                .timeWindow(300)
                .targetScope("ALL")
                .isEnabled(true)
                .build();
    }

    @Test
    @DisplayName("创建告警规则-成功")
    void createAlertRule_Success() {
        when(ruleRepository.findByRuleCode("RULE-001")).thenReturn(Optional.empty());
        when(ruleRepository.save(any(AlertRule.class))).thenAnswer(inv -> {
            AlertRule r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        AlertRule result = alertService.createAlertRule(rule);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(ruleRepository).save(any(AlertRule.class));
    }

    @Test
    @DisplayName("创建告警规则-编码已存在抛出异常")
    void createAlertRule_DuplicateCode_Throws() {
        when(ruleRepository.findByRuleCode("RULE-001")).thenReturn(Optional.of(rule));

        assertThrows(IllegalArgumentException.class, () -> alertService.createAlertRule(rule));
        verify(ruleRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新告警规则-成功")
    void updateAlertRule_Success() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(AlertRule.class))).thenReturn(rule);

        AlertRule result = alertService.updateAlertRule(rule);

        assertNotNull(result);
        verify(ruleRepository).save(rule);
    }

    @Test
    @DisplayName("更新告警规则-ID为空抛出异常")
    void updateAlertRule_NullId_Throws() {
        rule.setId(null);

        assertThrows(IllegalArgumentException.class, () -> alertService.updateAlertRule(rule));
        verify(ruleRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新告警规则-不存在抛出异常")
    void updateAlertRule_NotFound_Throws() {
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());
        rule.setId(999L);

        assertThrows(IllegalArgumentException.class, () -> alertService.updateAlertRule(rule));
    }

    @Test
    @DisplayName("根据ID查找规则-存在")
    void findRuleById_Exists() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        Optional<AlertRule> result = alertService.findRuleById(1L);

        assertTrue(result.isPresent());
        assertEquals("RULE-001", result.get().getRuleCode());
    }

    @Test
    @DisplayName("根据ID查找规则-不存在")
    void findRuleById_NotExists() {
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(alertService.findRuleById(999L).isEmpty());
    }

    @Test
    @DisplayName("根据编码查找规则-存在")
    void findRuleByCode_Exists() {
        when(ruleRepository.findByRuleCode("RULE-001")).thenReturn(Optional.of(rule));

        Optional<AlertRule> result = alertService.findRuleByCode("RULE-001");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("查找所有启用规则")
    void findAllEnabledRules() {
        when(ruleRepository.findByIsEnabledTrue()).thenReturn(List.of(rule));

        List<AlertRule> result = alertService.findAllEnabledRules();

        assertEquals(1, result.size());
        assertEquals("RULE-001", result.get(0).getRuleCode());
    }

    @Test
    @DisplayName("删除规则")
    void deleteRuleById() {
        doNothing().when(ruleRepository).deleteById(1L);

        assertDoesNotThrow(() -> alertService.deleteRuleById(1L));
        verify(ruleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("切换规则启用状态-成功")
    void toggleRuleEnabled_Success() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(AlertRule.class))).thenReturn(rule);

        AlertRule result = alertService.toggleRuleEnabled(1L, false);

        assertNotNull(result);
        assertFalse(result.getIsEnabled());
        verify(ruleRepository).save(argThat(r -> !r.getIsEnabled()));
    }

    @Test
    @DisplayName("切换规则启用状态-规则不存在抛出异常")
    void toggleRuleEnabled_NotFound_Throws() {
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                alertService.toggleRuleEnabled(999L, true));
    }

    @Test
    @DisplayName("检查告警规则-无启用规则不触发")
    void checkAlertRules_NoEnabledRules() {
        when(ruleRepository.findByIsEnabledTrue()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> alertService.checkAlertRules());
        verify(appLogRepository, never()).findAll(any(Specification.class));
        verify(recordRepository, never()).save(any());
    }

    @Test
    @DisplayName("检查告警规则-有规则但无匹配日志不告警")
    void checkAlertRules_NoMatchingLogs() {
        when(ruleRepository.findByIsEnabledTrue()).thenReturn(List.of(rule));
        when(appLogRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> alertService.checkAlertRules());
        verify(recordRepository, never()).save(any());
    }
}
