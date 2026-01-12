package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 需求管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface RequirementService {
    
    /**
     * 创建需求
     */
    TestRequirement createRequirement(TestRequirement requirement);
    
    /**
     * 更新需求
     */
    TestRequirement updateRequirement(Long id, TestRequirement requirement);
    
    /**
     * 根据ID查询需求
     */
    TestRequirement getRequirementById(Long id);
    
    /**
     * 分页查询需求列表
     * @param pageable 分页参数
     * @param requirementName 需求名称（模糊搜索，可选）
     * @param requirementStatus 需求状态（精确匹配，可选）
     */
    Page<TestRequirement> getRequirementList(Pageable pageable, String requirementName, String requirementStatus);
    
    /**
     * 删除需求
     */
    void deleteRequirement(Long id);
    
    /**
     * 更新需求状态
     */
    TestRequirement updateRequirementStatus(Long id, String status);
}

