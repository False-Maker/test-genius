package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Agent数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    /**
     * 根据Agent编码查询
     */
    Optional<Agent> findByAgentCode(String agentCode);
    
    /**
     * 根据启用状态查询
     */
    java.util.List<Agent> findByIsActive(String isActive);

    /**
     * 根据Agent类型查询
     */
    java.util.List<Agent> findByAgentTypeAndIsActive(String agentType, String isActive);
}

