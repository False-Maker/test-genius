package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.UIScriptTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UI脚本模板数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface UIScriptTemplateRepository extends JpaRepository<UIScriptTemplate, Long>, 
        JpaSpecificationExecutor<UIScriptTemplate> {
    
    /**
     * 根据模板编码查询
     */
    Optional<UIScriptTemplate> findByTemplateCode(String templateCode);
    
    /**
     * 查询指定前缀的模板编码列表（用于编码生成优化）
     */
    List<UIScriptTemplate> findByTemplateCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 根据模板类型查询启用的模板列表
     */
    List<UIScriptTemplate> findByTemplateTypeAndIsActive(String templateType, String isActive);
    
    /**
     * 根据脚本语言查询启用的模板列表
     */
    List<UIScriptTemplate> findByScriptLanguageAndIsActive(String scriptLanguage, String isActive);
    
    /**
     * 根据模板类型和脚本语言查询启用的模板列表
     */
    List<UIScriptTemplate> findByTemplateTypeAndScriptLanguageAndIsActive(
            String templateType, String scriptLanguage, String isActive);
    
    /**
     * 查询所有启用的模板
     */
    List<UIScriptTemplate> findByIsActive(String isActive);
    
    /**
     * 分页查询模板列表（带过滤条件）
     */
    @Query(value = "SELECT * FROM ui_script_template ust WHERE " +
            "(:templateName IS NULL OR ust.template_name ILIKE '%' || :templateName || '%') AND " +
            "(:templateType IS NULL OR ust.template_type = :templateType) AND " +
            "(:scriptLanguage IS NULL OR ust.script_language = :scriptLanguage) AND " +
            "(:isActive IS NULL OR ust.is_active = :isActive)",
            countQuery = "SELECT COUNT(*) FROM ui_script_template ust WHERE " +
            "(:templateName IS NULL OR ust.template_name ILIKE '%' || :templateName || '%') AND " +
            "(:templateType IS NULL OR ust.template_type = :templateType) AND " +
            "(:scriptLanguage IS NULL OR ust.script_language = :scriptLanguage) AND " +
            "(:isActive IS NULL OR ust.is_active = :isActive)",
            nativeQuery = true)
    Page<UIScriptTemplate> findWithFilters(
            @Param("templateName") String templateName,
            @Param("templateType") String templateType,
            @Param("scriptLanguage") String scriptLanguage,
            @Param("isActive") String isActive,
            Pageable pageable);
}

