package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.PromptTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板版本管理数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface PromptTemplateVersionRepository extends JpaRepository<PromptTemplateVersion, Long> {
    
    /**
     * 根据模板ID查询所有版本，按版本号降序
     */
    List<PromptTemplateVersion> findByTemplateIdOrderByVersionNumberDesc(Long templateId);
    
    /**
     * 根据模板ID和版本号查询
     */
    Optional<PromptTemplateVersion> findByTemplateIdAndVersionNumber(Long templateId, Integer versionNumber);
    
    /**
     * 查询当前版本
     */
    Optional<PromptTemplateVersion> findByTemplateIdAndIsCurrent(Long templateId, String isCurrent);
    
    /**
     * 更新指定模板的所有版本为非当前版本
     */
    @Modifying
    @Query("UPDATE PromptTemplateVersion pv SET pv.isCurrent = '0' WHERE pv.templateId = :templateId")
    void updateAllVersionsToNonCurrent(@Param("templateId") Long templateId);
    
    /**
     * 查询指定模板的最大版本号
     */
    @Query("SELECT COALESCE(MAX(pv.versionNumber), 0) FROM PromptTemplateVersion pv WHERE pv.templateId = :templateId")
    Integer findMaxVersionNumberByTemplateId(@Param("templateId") Long templateId);
}
