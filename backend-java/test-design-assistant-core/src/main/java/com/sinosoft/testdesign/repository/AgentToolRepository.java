package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AgentTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent工具数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface AgentToolRepository extends JpaRepository<AgentTool, Long> {
    
    /**
     * 根据工具编码查询
     */
    Optional<AgentTool> findByToolCode(String toolCode);
    
    /**
     * 根据工具类型查询
     */
    List<AgentTool> findByToolTypeAndIsActive(String toolType, String isActive);
    
    /**
     * 查询所有启用的工具
     */
    List<AgentTool> findByIsActive(String isActive);
    
    /**
     * 查询所有内置工具
     */
    List<AgentTool> findByIsBuiltin(String isBuiltin);
}

