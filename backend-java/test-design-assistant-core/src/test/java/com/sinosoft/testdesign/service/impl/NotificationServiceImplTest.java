package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.AlertRecord;
import com.sinosoft.testdesign.entity.AlertRule;
import com.sinosoft.testdesign.repository.AlertRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 通知服务单元测试（第四阶段 4.1）
 * 覆盖告警通知、邮件、站内信、批量通知等核心流程。
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("通知服务测试")
class NotificationServiceImplTest {

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private AlertRecord alertRecord;
    private AlertRule alertRule;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(notificationService, "mailEnabled", false);
        ReflectionTestUtils.setField(notificationService, "mailFrom", "");
        ReflectionTestUtils.setField(notificationService, "maxRetryAttempts", 2);
        ReflectionTestUtils.setField(notificationService, "retryDelaySeconds", 0);

        alertRecord = AlertRecord.builder()
                .id(1L)
                .ruleId(1L)
                .ruleCode("RULE-001")
                .alertLevel("WARNING")
                .alertTitle("失败率超标")
                .alertMessage("当前失败率 10%")
                .currentValue(new BigDecimal("10"))
                .thresholdValue(new BigDecimal("5"))
                .alertTime(LocalDateTime.now())
                .isResolved(false)
                .notificationSent(false)
                .build();

        alertRule = AlertRule.builder()
                .id(1L)
                .ruleCode("RULE-001")
                .ruleName("失败率告警")
                .ruleType("FAILURE_RATE")
                .thresholdValue(new BigDecimal("5"))
                .thresholdUnit("PERCENT")
                .notificationChannels("[\"EMAIL\",\"IN_APP\"]")
                .notificationRecipients("[{\"userId\":1},{\"email\":\"a@b.com\"}]")
                .build();
    }

    @Test
    @DisplayName("发送告警通知-规则不存在返回false")
    void sendAlertNotification_RuleNotFound_ReturnsFalse() {
        when(alertRuleRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = notificationService.sendAlertNotification(alertRecord);

        assertFalse(result);
    }

    @Test
    @DisplayName("发送告警通知-无接收人返回false")
    void sendAlertNotification_NoRecipients_ReturnsFalse() {
        when(alertRuleRepository.findById(1L)).thenReturn(Optional.of(alertRule));
        alertRule.setNotificationChannels("[\"EMAIL\"]");
        alertRule.setNotificationRecipients("");  // 空接收人，parseNotificationRecipients 返回 emptyList

        boolean result = notificationService.sendAlertNotification(alertRecord);

        assertFalse(result);
    }

    @Test
    @DisplayName("发送邮件-未启用邮件服务返回false")
    void sendEmail_MailDisabled_ReturnsFalse() {
        ReflectionTestUtils.setField(notificationService, "mailEnabled", false);

        boolean result = notificationService.sendEmail("a@b.com", "主题", "内容");

        assertFalse(result);
    }

    @Test
    @DisplayName("发送站内信-成功")
    void sendInAppMessage_Success() {
        boolean result = notificationService.sendInAppMessage(1L, "标题", "内容");

        assertTrue(result);
    }

    @Test
    @DisplayName("批量发送通知-规则不存在时各渠道均false")
    void sendBatchNotifications_RuleNotFound_AllFalse() {
        when(alertRuleRepository.findById(1L)).thenReturn(Optional.empty());

        Map<String, Boolean> result = notificationService.sendBatchNotifications(
                alertRecord, List.of("EMAIL", "IN_APP"));

        assertNotNull(result);
        assertFalse(result.get("EMAIL"));
        assertFalse(result.get("IN_APP"));
    }
}
