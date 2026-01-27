package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.PromptTemplateAbTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板A/B测试配置数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface PromptTemplateAbTestRepository extends JpaRepository<PromptTemplateAbTest, Long> {
    
    /**
     * 根据模板ID查询所有A/B测试
     */
    List<PromptTemplateAbTest> findByTemplateIdOrderByCreateTimeDesc(Long templateId);
    
    /**
     * 根据模板ID和状态查询A/B测试
     */
    List<PromptTemplateAbTest> findByTemplateIdAndStatus(Long templateId, String status);
    
    /**
     * 查询正在运行的A/B测试
     */
    @Query("SELECT ab FROM PromptTemplateAbTest ab WHERE ab.templateId = :templateId AND ab.status = 'running'")
    Optional<PromptTemplateAbTest> findRunningTestByTemplateId(@Param("templateId") Long templateId);
    
    /**
     * 查询所有正在运行的A/B测试
     */
    List<PromptTemplateAbTest> findByStatus(String status);
}
