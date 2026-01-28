package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.AgentSession;
import com.sinosoft.testdesign.repository.AgentSessionRepository;
import com.sinosoft.testdesign.service.AgentSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Agent会话服务实现
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Service
public class AgentSessionServiceImpl implements AgentSessionService {
    
    @Autowired
    private AgentSessionRepository sessionRepository;
    
    @Override
    public AgentSession createSession(AgentSession session) {
        // 自动生成会话编码
        if (session.getSessionCode() == null || session.getSessionCode().isEmpty()) {
            session.setSessionCode("SESSION-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase());
        }
        
        // 设置初始状态
        if (session.getStatus() == null || session.getStatus().isEmpty()) {
            session.setStatus("ACTIVE");
        }
        
        return sessionRepository.save(session);
    }
    
    @Override
    public Optional<AgentSession> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }
    
    @Override
    public Optional<AgentSession> getSessionByCode(String sessionCode) {
        return sessionRepository.findBySessionCode(sessionCode);
    }
    
    @Override
    public List<AgentSession> getSessionsByAgentId(Long agentId) {
        return sessionRepository.findByAgentId(agentId);
    }
    
    @Override
    public List<AgentSession> getSessionsByUserId(Long userId) {
        return sessionRepository.findByUserId(userId);
    }
    
    @Override
    public List<AgentSession> getSessionsByAgentIdAndUserId(Long agentId, Long userId) {
        return sessionRepository.findByAgentIdAndUserId(agentId, userId);
    }
    
    @Override
    public AgentSession closeSession(Long id) {
        Optional<AgentSession> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isPresent()) {
            AgentSession session = sessionOpt.get();
            session.setStatus("CLOSED");
            return sessionRepository.save(session);
        }
        throw new RuntimeException("会话不存在，ID: " + id);
    }
    
    @Override
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }
}

