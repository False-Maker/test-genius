package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 站内信实体
 *
 * @author sinosoft
 * @date 2026-01-28
 */
@Data
@Entity
@Table(name = "notification_message")
public class NotificationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接收用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 消息标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 是否已读
     */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 读取时间
     */
    @Column(name = "read_time")
    private LocalDateTime readTime;
}
