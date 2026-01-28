package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库数据访问接口
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long>, 
        JpaSpecificationExecutor<KnowledgeBase> {
    
    /**
     * 根据知识库编码查询
     * 
     * @param kbCode 知识库编码
     * @return 知识库实体
     */
    Optional<KnowledgeBase> findByKbCode(String kbCode);
    
    /**
     * 查询所有激活的知识库
     * 
     * @param 激活状态（0-未激活/1-已激活）
     * @return 知识库列表
     */
    List<KnowledgeBase> findByIsActive(String isActive);
    
    /**
     * 根据知识库类型查询
     * 
     * @param kbType 知识库类型（public/private/project）
     * @return 知识库列表
     */
    List<KnowledgeBase> findByKbType(String kbType);
    
    /**
     * 根据创建人查询知识库
     * 
     * @param creatorId 创建人ID
     * @return 知识库列表
     */
    List<KnowledgeBase> findByCreatorId(Long creatorId);
    
    /**
     * 查询指定前缀的知识库编码列表（用于编码生成优化）
     * 
     * @param prefix 编码前缀
     * @return 知识库列表
     */
    List<KnowledgeBase> findByKbCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 查询激活状态的知识库列表，按类型过滤
     * 
     * @param isActive 激活状态
     * @param kbType 知识库类型
     * @return 知识库列表
     */
    List<KnowledgeBase> findByIsActiveAndKbType(String isActive, String kbType);
}

