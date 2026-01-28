package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.Agent;
import com.sinosoft.testdesign.repository.AgentRepository;
import com.sinosoft.testdesign.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Agent服务实现
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Service
public class AgentServiceImpl implements AgentService {
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Override
    public Agent createAgent(Agent agent) {
        // 自动生成编码
        if (agent.getAgentCode() == null || agent.getAgentCode().isEmpty()) {
            String dateStr = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
            agent.setAgentCode("AGENT-" + dateStr);
        }
        
        return agentRepository.save(agent);
    }
    
    @Override
    public Agent updateAgent(Agent agent) {
        return agentRepository.save(agent);
    }
    
    @Override
    public Optional<Agent> getAgentById(Long id) {
        return agentRepository.findById(id);
    }
    
    @Override
    public Optional<Agent> getAgentByCode(String agentCode) {
        return agentRepository.findByAgentCode(agentCode);
    }
    
    @Override
    public List<Agent> getAllActiveAgents() {
        return agentRepository.findByIsActive("1");
    }
    
    @Override
    public List<Agent> getAgentsByType(String agentType) {
        return agentRepository.findByAgentTypeAndIsActive(agentType, "1");
    }
    
    @Override
    public void deleteAgent(Long id) {
        agentRepository.deleteById(id);
    }
    
    @Override
    public Agent toggleAgentActive(Long id, boolean active) {
        Optional<Agent> agentOpt = agentRepository.findById(id);
        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            agent.setIsActive(active ? "1" : "0");
            return agentRepository.save(agent);
        }
        throw new RuntimeException("Agent不存在，ID: " + id);
    }
}

