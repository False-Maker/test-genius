package com.sinosoft.testdesign.service;

/**
 * 智能用例生成服务接口
 * 具体实现可后续开发
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface IntelligentCaseGenerationService {
    
    /**
     * 生成测试用例
     * @param request 用例生成请求
     * @return 用例生成结果
     */
    // CaseGenerationResult generateTestCases(CaseGenerationRequest request);
    
    /**
     * 批量生成测试用例
     * @param request 批量生成请求
     * @return 批量生成结果
     */
    // BatchGenerationResult batchGenerateTestCases(BatchGenerationRequest request);
    
    /**
     * 优化测试用例
     * @param caseId 用例ID
     * @return 优化后的用例
     */
    // TestCase optimizeTestCase(Long caseId);
}

