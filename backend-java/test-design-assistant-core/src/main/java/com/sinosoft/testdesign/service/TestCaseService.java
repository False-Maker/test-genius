package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 测试用例管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface TestCaseService {
    
    /**
     * 创建用例
     */
    TestCase createTestCase(TestCase testCase);
    
    /**
     * 更新用例
     */
    TestCase updateTestCase(Long id, TestCase testCase);
    
    /**
     * 根据ID查询用例
     */
    TestCase getTestCaseById(Long id);
    
    /**
     * 分页查询用例列表
     * @param pageable 分页参数
     * @param caseName 用例名称（模糊搜索，可选）
     * @param caseStatus 用例状态（精确匹配，可选）
     * @param requirementId 需求ID（精确匹配，可选）
     */
    Page<TestCase> getTestCaseList(Pageable pageable, String caseName, String caseStatus, Long requirementId);
    
    /**
     * 删除用例
     */
    void deleteTestCase(Long id);
    
    /**
     * 更新用例状态
     */
    TestCase updateCaseStatus(Long id, String status);
    
    /**
     * 审核用例
     */
    TestCase reviewTestCase(Long id, String reviewResult, String reviewComment);
}

