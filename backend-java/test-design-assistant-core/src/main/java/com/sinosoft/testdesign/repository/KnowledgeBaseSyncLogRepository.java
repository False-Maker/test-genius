package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.KnowledgeBaseSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库同步日志数据访问接口
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Repository
public interface KnowledgeBaseSyncLogRepository extends JpaRepository<KnowledgeBaseSyncLog, Long>, 
        JpaSpecificationExecutor<KnowledgeBaseSyncLog> {
    
    /**
     * 查询指定知识库的所有同步日志
     * 
     * @param kbId 知识库ID
     * @return 同步日志列表
     */
    List<KnowledgeBaseSyncLog> findByKbIdOrderByCreateTimeDesc(Long kbId);
    
    /**
     * 查询指定知识库的指定同步类型的日志
     * 
     * @param kbId 知识库ID
     * @param syncType 同步类型（incremental/full）
     * @return 同步日志列表
     */
    List<KnowledgeBaseSyncLog> findByKbIdAndSyncTypeOrderByCreateTimeDesc(Long kbId, String syncType);
    
    /**
     * 查询指定知识库的运行中或待处理的同步任务
     * 
     * @param kbId 知识库ID
     * @return 同步日志列表
     */
    List<KnowledgeBaseSyncLog> findByKbIdAndStatusInOrderByCreateTimeDesc(Long kbId, List<String> status);
    
    /**
     * 查询最近的同步记录
     * 
     * @param kbId 知识库ID
     * @return 最近的一条同步记录
     */
    Optional<KnowledgeBaseSyncLog> findTopByKbIdOrderByCreateTimeDesc(Long kbId);
    
    /**
     * 统计指定知识库的成功同步次数
     * 
     * @param kbId 知识库ID
     * @return 成功同步次数
     */
    long countByKbIdAndStatus(Long kbId, String status);
}

