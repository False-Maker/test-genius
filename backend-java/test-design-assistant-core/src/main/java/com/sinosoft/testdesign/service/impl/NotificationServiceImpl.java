package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.AlertRecord;
import com.sinosoft.testdesign.entity.AlertRule;
import com.sinosoft.testdesign.entity.NotificationMessage;
import com.sinosoft.testdesign.repository.AlertRuleRepository;
import com.sinosoft.testdesign.repository.NotificationMessageRepository;
import com.sinosoft.testdesign.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 * 
 * @author sinosoft
 * @date 2026-01-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationMessageRepository notificationMessageRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private JavaMailSender mailSender; // 可选，如果配置了邮件服务
    
    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;
    
    @Value("${spring.mail.from:}")
    private String mailFrom;
    
    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${notification.retry.delay-seconds:5}")
    private int retryDelaySeconds;
    
    /**
     * 发送告警通知
     */
    @Override
    @Transactional
    public boolean sendAlertNotification(AlertRecord alertRecord) {
        try {
            // 获取告警规则
            Optional<AlertRule> ruleOpt = alertRuleRepository.findById(alertRecord.getRuleId());
            if (ruleOpt.isEmpty()) {
                log.warn("告警规则不存在: ruleId={}", alertRecord.getRuleId());
                return false;
            }
            
            AlertRule rule = ruleOpt.get();
            
            // 解析通知渠道
            List<String> channels = parseNotificationChannels(rule.getNotificationChannels());
            if (channels.isEmpty()) {
                log.warn("告警规则未配置通知渠道: ruleCode={}", rule.getRuleCode());
                return false;
            }
            
            // 解析接收人
            List<NotificationRecipient> recipients = parseNotificationRecipients(rule.getNotificationRecipients());
            if (recipients.isEmpty()) {
                log.warn("告警规则未配置接收人: ruleCode={}", rule.getRuleCode());
                return false;
            }
            
            // 批量发送通知
            Map<String, Boolean> results = sendBatchNotifications(alertRecord, channels);
            
            // 更新告警记录的发送状态
            boolean allSuccess = results.values().stream().allMatch(Boolean::booleanValue);
            if (allSuccess) {
                alertRecord.setNotificationSent(true);
                alertRecord.setNotificationChannels(String.join(",", channels));
                log.info("告警通知发送成功: alertId={}, channels={}", alertRecord.getId(), channels);
            } else {
                log.warn("部分通知渠道发送失败: alertId={}, results={}", alertRecord.getId(), results);
            }
            
            return allSuccess;
            
        } catch (Exception e) {
            log.error("发送告警通知失败: alertId={}", alertRecord.getId(), e);
            return false;
        }
    }
    
    /**
     * 发送邮件通知
     */
    @Override
    public boolean sendEmail(String to, String subject, String content) {
        if (!mailEnabled) {
            log.warn("邮件服务未启用，跳过邮件发送: to={}", to);
            return false;
        }
        
        if (mailSender == null) {
            log.warn("邮件发送器未配置，跳过邮件发送: to={}", to);
            return false;
        }
        
        return retry(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailFrom);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(content);
                
                mailSender.send(message);
                log.info("邮件发送成功: to={}, subject={}", to, subject);
                return true;
            } catch (Exception e) {
                log.error("邮件发送失败: to={}, subject={}", to, subject, e);
                throw e;
            }
        });
    }
    
    /**
     * 发送站内信通知
     */
    @Override
    @Transactional
    public boolean sendInAppMessage(Long userId, String title, String content) {
        return retry(() -> {
            try {
                NotificationMessage message = new NotificationMessage();
                message.setUserId(userId);
                message.setTitle(title);
                message.setContent(content);
                message.setIsRead(false);
                
                notificationMessageRepository.save(message);
                
                log.info("站内信通知已保存: userId={}, title={}", userId, title);
                return true;
            } catch (Exception e) {
                log.error("站内信发送失败: userId={}, title={}", userId, title, e);
                throw e;
            }
        });
    }
    
    /**
     * 批量发送通知
     */
    @Override
    public Map<String, Boolean> sendBatchNotifications(AlertRecord alertRecord, List<String> channels) {
        Map<String, Boolean> results = new HashMap<>();
        
        // 获取告警规则和接收人信息
        Optional<AlertRule> ruleOpt = alertRuleRepository.findById(alertRecord.getRuleId());
        if (ruleOpt.isEmpty()) {
            log.warn("告警规则不存在: ruleId={}", alertRecord.getRuleId());
            channels.forEach(channel -> results.put(channel, false));
            return results;
        }
        
        AlertRule rule = ruleOpt.get();
        List<NotificationRecipient> recipients = parseNotificationRecipients(rule.getNotificationRecipients());
        
        // 构建通知内容
        String subject = buildNotificationSubject(alertRecord);
        String content = buildNotificationContent(alertRecord, rule);
        
        // 并行发送不同渠道的通知
        List<CompletableFuture<Map.Entry<String, Boolean>>> futures = new ArrayList<>();
        
        for (String channel : channels) {
            CompletableFuture<Map.Entry<String, Boolean>> future = CompletableFuture.supplyAsync(() -> {
                boolean success = false;
                try {
                    switch (channel.toUpperCase()) {
                        case "EMAIL":
                            success = sendEmailNotifications(recipients, subject, content);
                            break;
                        case "IN_APP":
                            success = sendInAppNotifications(recipients, subject, content);
                            break;
                        case "SMS":
                            success = sendSmsNotifications(recipients, content);
                            break;
                        case "WEBHOOK":
                            success = sendWebhookNotifications(alertRecord, rule);
                            break;
                        default:
                            log.warn("未知的通知渠道: {}", channel);
                            success = false;
                    }
                } catch (Exception e) {
                    log.error("发送{}通知失败", channel, e);
                    success = false;
                }
                return Map.entry(channel, success);
            });
            futures.add(future);
        }
        
        // 等待所有通知发送完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 收集结果
        futures.forEach(future -> {
            try {
                Map.Entry<String, Boolean> entry = future.get();
                results.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("获取通知发送结果失败", e);
            }
        });
        
        return results;
    }
    
    /**
     * 发送邮件通知（批量）
     */
    private boolean sendEmailNotifications(List<NotificationRecipient> recipients, String subject, String content) {
        boolean allSuccess = true;
        for (NotificationRecipient recipient : recipients) {
            if (recipient.getEmail() != null && !recipient.getEmail().isEmpty()) {
                boolean success = sendEmail(recipient.getEmail(), subject, content);
                if (!success) {
                    allSuccess = false;
                }
            }
        }
        return allSuccess;
    }
    
    /**
     * 发送站内信通知（批量）
     */
    private boolean sendInAppNotifications(List<NotificationRecipient> recipients, String title, String content) {
        boolean allSuccess = true;
        // 获取当前代理对象，确保事务生效
        NotificationService self = applicationContext.getBean(NotificationService.class);
        
        for (NotificationRecipient recipient : recipients) {
            if (recipient.getUserId() != null) {
                // 调用代理对象的方法
                boolean success = self.sendInAppMessage(recipient.getUserId(), title, content);
                if (!success) {
                    allSuccess = false;
                }
            }
        }
        return allSuccess;
    }
    
    /**
     * 发送短信通知（模拟实现）
     */
    private boolean sendSmsNotifications(List<NotificationRecipient> recipients, String content) {
        // 由于没有真实短信网关，这里进行模拟发送并记录日志
        log.info("开始发送短信通知，接收人数量: {}", recipients.size());
        
        boolean allSuccess = true;
        for (NotificationRecipient recipient : recipients) {
            if (recipient.getPhone() != null && !recipient.getPhone().isEmpty()) {
                try {
                    // 模拟发送延迟
                    Thread.sleep(50);
                    log.info("模拟发送短信成功: phone={}, content={}", recipient.getPhone(), content);
                } catch (Exception e) {
                    log.error("模拟发送短信失败: phone={}", recipient.getPhone(), e);
                    allSuccess = false;
                }
            }
        }
        return allSuccess;
    }
    
    /**
     * 发送Webhook通知
     */
    private boolean sendWebhookNotifications(AlertRecord alertRecord, AlertRule rule) {
        String webhookUrl = rule.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Webhook URL未配置: ruleCode={}", rule.getRuleCode());
            return false;
        }
        
        log.info("开始发送Webhook通知: url={}", webhookUrl);
        
        return retry(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("alertId", alertRecord.getId());
                payload.put("ruleName", rule.getRuleName());
                payload.put("level", alertRecord.getAlertLevel());
                payload.put("title", alertRecord.getAlertTitle());
                payload.put("message", alertRecord.getAlertMessage());
                payload.put("time", alertRecord.getAlertTime());
                payload.put("currentValue", alertRecord.getCurrentValue());
                
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                
                restTemplate.postForEntity(webhookUrl, entity, String.class);
                log.info("Webhook通知发送成功: url={}", webhookUrl);
                return true;
            } catch (Exception e) {
                log.error("Webhook通知发送失败: url={}", webhookUrl, e);
                throw e;
            }
        });
    }
    
    /**
     * 构建通知主题
     */
    private String buildNotificationSubject(AlertRecord alertRecord) {
        return String.format("[%s] %s", alertRecord.getAlertLevel(), alertRecord.getAlertTitle());
    }
    
    /**
     * 构建通知内容
     */
    private String buildNotificationContent(AlertRecord alertRecord, AlertRule rule) {
        StringBuilder content = new StringBuilder();
        content.append("告警信息：\n");
        content.append("规则名称：").append(rule.getRuleName()).append("\n");
        content.append("告警级别：").append(alertRecord.getAlertLevel()).append("\n");
        content.append("告警标题：").append(alertRecord.getAlertTitle()).append("\n");
        content.append("告警消息：").append(alertRecord.getAlertMessage()).append("\n");
        content.append("当前值：").append(alertRecord.getCurrentValue()).append(" ").append(rule.getThresholdUnit()).append("\n");
        content.append("阈值：").append(alertRecord.getThresholdValue()).append(" ").append(rule.getThresholdUnit()).append("\n");
        content.append("告警时间：").append(alertRecord.getAlertTime()).append("\n");
        
        if (alertRecord.getTargetScope() != null) {
            content.append("目标范围：").append(alertRecord.getTargetScope()).append("\n");
        }
        if (alertRecord.getTargetValue() != null) {
            content.append("目标值：").append(alertRecord.getTargetValue()).append("\n");
        }
        
        return content.toString();
    }
    
    /**
     * 解析通知渠道
     */
    private List<String> parseNotificationChannels(String channelsJson) {
        if (channelsJson == null || channelsJson.isEmpty()) {
            return Arrays.asList("EMAIL", "IN_APP"); // 默认渠道
        }
        
        try {
            List<String> channels = objectMapper.readValue(channelsJson, new TypeReference<List<String>>() {});
            return channels != null ? channels : Collections.emptyList();
        } catch (Exception e) {
            log.warn("解析通知渠道失败，使用默认渠道: {}", channelsJson, e);
            return Arrays.asList("EMAIL", "IN_APP");
        }
    }
    
    /**
     * 解析通知接收人
     */
    private List<NotificationRecipient> parseNotificationRecipients(String recipientsJson) {
        if (recipientsJson == null || recipientsJson.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            List<Map<String, Object>> recipients = objectMapper.readValue(recipientsJson, new TypeReference<List<Map<String, Object>>>() {});
            return recipients.stream()
                    .map(map -> {
                        NotificationRecipient recipient = new NotificationRecipient();
                        if (map.containsKey("userId")) {
                            recipient.setUserId(Long.valueOf(map.get("userId").toString()));
                        }
                        if (map.containsKey("email")) {
                            recipient.setEmail(map.get("email").toString());
                        }
                        if (map.containsKey("phone")) {
                            recipient.setPhone(map.get("phone").toString());
                        }
                        return recipient;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("解析通知接收人失败: {}", recipientsJson, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 重试机制
     */
    private boolean retry(java.util.function.Supplier<Boolean> supplier) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetryAttempts) {
            try {
                return supplier.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryDelaySeconds * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        if (lastException != null) {
            log.error("重试{}次后仍然失败", maxRetryAttempts, lastException);
        }
        return false;
    }
    
    /**
     * 通知接收人内部类
     */
    private static class NotificationRecipient {
        private Long userId;
        private String email;
        private String phone;
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
