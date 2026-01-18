package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestReportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 测试报告模板管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface TestReportTemplateService {
    
    /**
     * 创建报告模板
     */
    TestReportTemplate createTemplate(TestReportTemplate template);
    
    /**
     * 更新报告模板
     */
    TestReportTemplate updateTemplate(Long id, TestReportTemplate template);
    
    /**
     * 根据ID查询模板
     */
    TestReportTemplate getTemplateById(Long id);
    
    /**
     * 根据编码查询模板
     */
    TestReportTemplate getTemplateByCode(String templateCode);
    
    /**
     * 分页查询模板列表
     */
    Page<TestReportTemplate> getTemplateList(Pageable pageable);
    
    /**
     * 删除模板
     */
    void deleteTemplate(Long id);
    
    /**
     * 启用/禁用模板
     */
    TestReportTemplate toggleTemplateStatus(Long id, String isActive);
    
    /**
     * 设置默认模板
     */
    TestReportTemplate setDefaultTemplate(Long id, String templateType);
    
    /**
     * 根据类型查询启用的模板列表
     */
    java.util.List<TestReportTemplate> getActiveTemplatesByType(String templateType);
    
    /**
     * 根据类型查询默认模板
     */
    TestReportTemplate getDefaultTemplateByType(String templateType);
}

