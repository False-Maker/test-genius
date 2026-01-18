package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 测试规约管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface TestSpecificationService {
    
    /**
     * 创建测试规约
     */
    TestSpecification createSpecification(TestSpecification specification);
    
    /**
     * 更新测试规约
     */
    TestSpecification updateSpecification(Long id, TestSpecification specification);
    
    /**
     * 根据ID查询测试规约
     */
    TestSpecification getSpecificationById(Long id);
    
    /**
     * 根据编码查询测试规约
     */
    TestSpecification getSpecificationByCode(String specCode);
    
    /**
     * 分页查询测试规约列表
     * @param pageable 分页参数
     * @param specName 规约名称（模糊搜索，可选）
     * @param specType 规约类型（精确匹配，可选）：APPLICATION/PUBLIC
     * @param isActive 是否启用（精确匹配，可选）：1-启用，0-禁用
     */
    Page<TestSpecification> getSpecificationList(Pageable pageable, String specName, String specType, String isActive);
    
    /**
     * 删除测试规约
     */
    void deleteSpecification(Long id);
    
    /**
     * 启用/禁用测试规约
     */
    TestSpecification updateSpecificationStatus(Long id, String isActive);
    
    /**
     * 查询应用级规约列表
     */
    List<TestSpecification> getApplicationSpecifications();
    
    /**
     * 查询公共规约列表
     */
    List<TestSpecification> getPublicSpecifications();
    
    /**
     * 根据适用模块查询规约
     */
    List<TestSpecification> getSpecificationsByModule(String module);
    
    /**
     * 根据适用测试分层查询规约
     */
    List<TestSpecification> getSpecificationsByLayer(String layer);
    
    /**
     * 创建规约版本
     */
    TestSpecification createVersion(Long specId, String versionNumber, String versionName, 
                                   String versionDescription, String changeLog);
    
    /**
     * 切换规约版本
     */
    TestSpecification switchVersion(Long specId, String versionNumber);
    
    /**
     * 查询规约版本列表
     */
    List<com.sinosoft.testdesign.entity.SpecVersion> getVersionList(Long specId);
}

