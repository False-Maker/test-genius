package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent会话数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, Long> {
    
    /**
     * 根据会话编码查询
     */
    Optional<AgentSession> findBySessionCode(String sessionCode);
    
    /**
     * 根据Agent ID查询会话列表
     */
    List<AgentSession> findByAgentId(Long agentId);
    
    /**
     * 根据用户ID查询会话列表
     */
    List<AgentSession> findByUserId(Long userId);
    
    /**
     * 根据Agent ID和用户ID查询会话列表
     */
    @Query("SELECT s FROM AgentSession s WHERE s.agentId = :agentId AND s.userId = :userId ORDER BY s.lastActiveTime DESC")
    List<AgentSession> findByAgentIdAndUserId(@Param("agentId") Long agentId, @Param("userId") Long userId);
    
    /**
     * 根据状态查询会话列表
     */
    List<AgentSession> findByStatus(String status);
}

