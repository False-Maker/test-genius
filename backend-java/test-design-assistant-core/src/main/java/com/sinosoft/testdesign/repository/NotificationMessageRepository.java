package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 站内信仓库
 *
 * @author sinosoft
 * @date 2026-01-28
 */
@Repository
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {
    
    /**
     * 查询用户的消息列表
     */
    List<NotificationMessage> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * 查询用户的未读消息数量
     */
    long countByUserIdAndIsReadFalse(Long userId);
}
