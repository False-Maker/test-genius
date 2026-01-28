package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AgentToolCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent工具调用记录数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface AgentToolCallRepository extends JpaRepository<AgentToolCall, Long> {
    
    /**
     * 根据会话ID查询工具调用记录
     */
    @Query("SELECT c FROM AgentToolCall c WHERE c.sessionId = :sessionId ORDER BY c.createTime DESC")
    List<AgentToolCall> findBySessionIdOrderByCreateTimeDesc(@Param("sessionId") Long sessionId);
    
    /**
     * 根据工具编码查询调用记录
     */
    List<AgentToolCall> findByToolCode(String toolCode);
    
    /**
     * 根据调用状态查询
     */
    List<AgentToolCall> findByCallStatus(String callStatus);
}

