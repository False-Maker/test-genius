package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.Agent;
import com.sinosoft.testdesign.repository.AgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Agent服务接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
public interface AgentService {
    
    /**
     * 创建Agent
     */
    Agent createAgent(Agent agent);
    
    /**
     * 更新Agent
     */
    Agent updateAgent(Agent agent);
    
    /**
     * 根据ID查询Agent
     */
    Optional<Agent> getAgentById(Long id);
    
    /**
     * 根据编码查询Agent
     */
    Optional<Agent> getAgentByCode(String agentCode);
    
    /**
     * 查询所有启用的Agent
     */
    List<Agent> getAllActiveAgents();
    
    /**
     * 查询指定类型的Agent
     */
    List<Agent> getAgentsByType(String agentType);
    
    /**
     * 删除Agent
     */
    void deleteAgent(Long id);
    
    /**
     * 启用/禁用Agent
     */
    Agent toggleAgentActive(Long id, boolean active);
}

