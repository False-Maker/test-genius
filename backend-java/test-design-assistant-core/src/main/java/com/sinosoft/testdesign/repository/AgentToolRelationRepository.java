package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AgentToolRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent工具关联数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface AgentToolRelationRepository extends JpaRepository<AgentToolRelation, Long> {
    
    /**
     * 根据Agent ID查询工具关联列表
     */
    @Query("SELECT r FROM AgentToolRelation r WHERE r.agentId = :agentId AND r.isEnabled = '1' ORDER BY r.toolOrder ASC")
    List<AgentToolRelation> findByAgentIdAndEnabled(@Param("agentId") Long agentId);
    
    /**
     * 根据工具ID查询Agent关联列表
     */
    List<AgentToolRelation> findByToolId(Long toolId);
    
    /**
     * 删除Agent的所有工具关联
     */
    void deleteByAgentId(Long agentId);
}

