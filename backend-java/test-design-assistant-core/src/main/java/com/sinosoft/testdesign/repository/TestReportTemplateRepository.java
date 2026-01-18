package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试报告模板数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface TestReportTemplateRepository extends JpaRepository<TestReportTemplate, Long>, 
        JpaSpecificationExecutor<TestReportTemplate> {
    
    /**
     * 根据模板编码查询
     */
    Optional<TestReportTemplate> findByTemplateCode(String templateCode);
    
    /**
     * 查询所有启用的模板
     */
    List<TestReportTemplate> findByIsActive(String isActive);
    
    /**
     * 根据模板类型查询启用的模板
     */
    List<TestReportTemplate> findByTemplateTypeAndIsActive(String templateType, String isActive);
    
    /**
     * 查询默认模板
     */
    Optional<TestReportTemplate> findByTemplateTypeAndIsDefaultAndIsActive(
            String templateType, String isDefault, String isActive);
    
    /**
     * 查询指定前缀的模板编码列表（用于编码生成优化）
     */
    List<TestReportTemplate> findByTemplateCodeStartingWithOrderByIdDesc(String prefix);
}

