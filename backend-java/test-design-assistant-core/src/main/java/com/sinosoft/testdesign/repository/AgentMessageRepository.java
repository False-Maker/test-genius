package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AgentMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent消息数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface AgentMessageRepository extends JpaRepository<AgentMessage, Long> {
    
    /**
     * 根据会话ID查询消息列表（按时间排序）
     */
    @Query("SELECT m FROM AgentMessage m WHERE m.sessionId = :sessionId ORDER BY m.createTime ASC")
    List<AgentMessage> findBySessionIdOrderByCreateTimeAsc(@Param("sessionId") Long sessionId);
    
    /**
     * 根据会话ID和消息类型查询
     */
    List<AgentMessage> findBySessionIdAndMessageType(Long sessionId, String messageType);
}

