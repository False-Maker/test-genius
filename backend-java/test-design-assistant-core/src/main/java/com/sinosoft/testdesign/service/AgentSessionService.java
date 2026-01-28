package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.AgentSession;
import java.util.List;
import java.util.Optional;

/**
 * Agent会话服务接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
public interface AgentSessionService {
    
    /**
     * 创建会话
     */
    AgentSession createSession(AgentSession session);
    
    /**
     * 根据ID查询会话
     */
    Optional<AgentSession> getSessionById(Long id);
    
    /**
     * 根据编码查询会话
     */
    Optional<AgentSession> getSessionByCode(String sessionCode);
    
    /**
     * 查询Agent的所有会话
     */
    List<AgentSession> getSessionsByAgentId(Long agentId);
    
    /**
     * 查询用户的所有会话
     */
    List<AgentSession> getSessionsByUserId(Long userId);
    
    /**
     * 查询Agent和用户的所有会话
     */
    List<AgentSession> getSessionsByAgentIdAndUserId(Long agentId, Long userId);
    
    /**
     * 关闭会话
     */
    AgentSession closeSession(Long id);
    
    /**
     * 删除会话
     */
    void deleteSession(Long id);
}

