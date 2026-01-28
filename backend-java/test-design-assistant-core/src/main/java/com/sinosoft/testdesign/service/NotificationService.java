package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.AlertRecord;

/**
 * 通知服务接口
 * 负责发送告警通知（邮件、站内信等）
 * 
 * @author sinosoft
 * @date 2026-01-28
 */
public interface NotificationService {
    
    /**
     * 发送告警通知
     * 
     * @param alertRecord 告警记录
     * @return 是否发送成功
     */
    boolean sendAlertNotification(AlertRecord alertRecord);
    
    /**
     * 发送邮件通知
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    boolean sendEmail(String to, String subject, String content);
    
    /**
     * 发送站内信通知
     * 
     * @param userId 用户ID
     * @param title 消息标题
     * @param content 消息内容
     * @return 是否发送成功
     */
    boolean sendInAppMessage(Long userId, String title, String content);
    
    /**
     * 批量发送通知
     * 
     * @param alertRecord 告警记录
     * @param channels 通知渠道列表（EMAIL, IN_APP, SMS, WEBHOOK等）
     * @return 发送结果映射（渠道 -> 是否成功）
     */
    java.util.Map<String, Boolean> sendBatchNotifications(AlertRecord alertRecord, java.util.List<String> channels);
}
