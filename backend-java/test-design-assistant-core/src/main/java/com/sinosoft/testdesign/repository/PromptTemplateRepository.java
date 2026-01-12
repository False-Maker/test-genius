package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long>, 
        JpaSpecificationExecutor<PromptTemplate> {
    
    /**
     * 根据模板编码查询
     */
    Optional<PromptTemplate> findByTemplateCode(String templateCode);
    
    /**
     * 查询所有启用的模板
     */
    List<PromptTemplate> findByIsActive(String isActive);
    
    /**
     * 查询指定前缀的模板编码列表（用于编码生成优化）
     */
    List<PromptTemplate> findByTemplateCodeStartingWithOrderByIdDesc(String prefix);
}

